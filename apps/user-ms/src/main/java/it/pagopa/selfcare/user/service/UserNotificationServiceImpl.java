package it.pagopa.selfcare.user.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import it.pagopa.selfcare.product.entity.Product;
import it.pagopa.selfcare.product.entity.ProductRole;
import it.pagopa.selfcare.user.client.eventhub.EventHubRestClient;
import it.pagopa.selfcare.user.conf.CloudTemplateLoader;
import it.pagopa.selfcare.user.constant.OnboardedProductState;
import it.pagopa.selfcare.user.entity.OnboardedProduct;
import it.pagopa.selfcare.user.entity.UserInstitution;
import it.pagopa.selfcare.user.exception.InvalidRequestException;
import it.pagopa.selfcare.user.model.LoggedUser;
import it.pagopa.selfcare.user.model.notification.UserNotificationToSend;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.openapi.quarkus.user_registry_json.model.CertifiableFieldResourceOfstring;
import org.openapi.quarkus.user_registry_json.model.UserResource;
import org.openapi.quarkus.user_registry_json.model.WorkContactResource;
import software.amazon.awssdk.utils.CollectionUtils;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static it.pagopa.selfcare.user.constant.TemplateMailConstant.*;


@Slf4j
@ApplicationScoped
public class UserNotificationServiceImpl implements UserNotificationService {

    @RestClient
    @Inject
    EventHubRestClient eventHubRestClient;

    private final MailService mailService;
    private final Configuration freemarkerConfig;
    private final boolean eventHubUsersEnabled;


    public UserNotificationServiceImpl(Configuration freemarkerConfig, CloudTemplateLoader cloudTemplateLoader, MailService mailService,
                                       @ConfigProperty(name = "user-ms.eventhub.users.enabled") boolean eventHubUsersEnabled) {
        this.mailService = mailService;
        this.freemarkerConfig = freemarkerConfig;
        freemarkerConfig.setTemplateLoader(cloudTemplateLoader);
        this.eventHubUsersEnabled = eventHubUsersEnabled;
    }

    @Override
    public Uni<UserNotificationToSend> sendKafkaNotification(UserNotificationToSend userNotificationToSend, String userId) {
        return eventHubUsersEnabled
            ? eventHubRestClient.sendMessage(userNotificationToSend)
                .onItem().invoke(() -> log.info("sent dataLake notification for user : {}", userId))
                .onFailure().invoke(throwable -> log.warn("error during send dataLake notification for user {}: {} ", userId, throwable.getMessage(), throwable))
                .replaceWith(userNotificationToSend)
            : Uni.createFrom().item(userNotificationToSend);
    }

    @Override
    public Uni<Void> sendEmailNotification(UserResource user, UserInstitution institution, Product product, OnboardedProductState status, String loggedUserName, String loggedUserSurname) {
        log.info("sendMailNotification {}", status.name());
        return switch (status) {
            case ACTIVE ->
                    buildDataModelAndSendEmail(user, institution, product, ACTIVATE_TEMPLATE, ACTIVATE_SUBJECT, loggedUserName, loggedUserSurname);
            case DELETED ->
                    buildDataModelAndSendEmail(user, institution, product, DELETE_TEMPLATE, DELETE_SUBJECT, loggedUserName, loggedUserSurname);
            case SUSPENDED ->
                    buildDataModelAndSendEmail(user, institution, product, SUSPEND_TEMPLATE, SUSPEND_SUBJECT, loggedUserName, loggedUserSurname);
            case PENDING, TOBEVALIDATED, REJECTED -> Uni.createFrom().voidItem();
        };
    }

    @Override
    public Uni<Void> sendCreateUserNotification(String institutionDescription, List<String> roleLabels, UserResource userResource, UserInstitution userInstitution, Product product, LoggedUser loggedUser) {
        log.debug("sendCreateNotification start");
        log.debug("sendCreateNotification institution = {}, productTitle = {}, email = {}", institutionDescription, product.getTitle(), userInstitution.getUserMailUuid());

        String email = retrieveMail(userResource, userInstitution);
        String templateName = roleLabels.size() > 1 ? CREATE_TEMPLATE_MULTIPLE_ROLE : CREATE_TEMPLATE_SINGLE_ROLE;
        Map<String, String> dataModel = buildCreateEmailDataModel(loggedUser, product, institutionDescription, roleLabels);

        return this.sendEmailNotification(templateName, CREATE_SUBJECT, email, dataModel)
                .onItem().invoke(() -> log.debug("sendCreateNotification end"));
    }

    private Map<String, String> buildCreateEmailDataModel(LoggedUser loggedUser, Product product, String institutionDescription, List<String> roleLabels) {
        Map<String, String> dataModel = new HashMap<>();
        dataModel.put("requesterName", loggedUser.getName());
        dataModel.put("requesterSurname", loggedUser.getFamilyName());

        dataModel.put("productName", product.getTitle());
        dataModel.put("institutionName", institutionDescription);
        if (roleLabels.size() > 1) {
            String roleLabel = roleLabels.stream()
                    .limit(roleLabels.size() - 1L)
                    .collect(Collectors.joining(", "));

            dataModel.put("productRoles", roleLabel);
            dataModel.put("lastProductRole", roleLabels.get(roleLabels.size() - 1));
        } else {
            String roleLabel = roleLabels.get(0);
            dataModel.put("productRole", roleLabel);
        }

        return dataModel;
    }
    private Map<String, String> buildEmailDataModel(UserInstitution institution, Product product, String loggedUserName, String loggedUserSurname) {
        Optional<OnboardedProduct> productDb = institution.getProducts().stream().filter(p -> StringUtils.equals(p.getProductId(), product.getId())).findFirst();

        Optional<String> roleLabel = Optional.empty();
        if (productDb.isPresent()) {
            roleLabel = product.getRoleMappings().values().stream()
                    .flatMap(productRoleInfo -> productRoleInfo.getRoles().stream())
                    .filter(productRole -> productRole.getCode().equals(productDb.get().getProductRole()))
                    .map(ProductRole::getLabel).findAny();
        }

        Map<String, String> dataModel = new HashMap<>();
        dataModel.put("productName", product.getTitle());
        dataModel.put("productRole", roleLabel.orElse("no_role_found"));
        dataModel.put("institutionName", institution.getInstitutionDescription());
        dataModel.put("requesterName", loggedUserName);
        dataModel.put("requesterSurname", loggedUserSurname);
        return dataModel;
    }

    private Uni<Void> buildDataModelAndSendEmail(org.openapi.quarkus.user_registry_json.model.UserResource user, UserInstitution institution, Product product, String templateName, String subject, String loggedUserName, String loggedUserSurname) {
        String email = retrieveMail(user, institution);
        Map<String, String> dataModel = buildEmailDataModel(institution, product, loggedUserName, loggedUserSurname);
        return this.sendEmailNotification(templateName, subject, email, dataModel);
    }

    private Uni<Void> sendEmailNotification(String templateName, String subject, String email, Map<String, String> dataModel) {
        return Uni.createFrom().item(getContent(templateName, dataModel))
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                .onItem().transformToUni(content -> mailService.sendMail(email, content.toString(), subject));
    }

    private StringWriter getContent(String templateName, Map<String, String> dataModel) {
        StringWriter stringWriter = null;
        try {
            Template template = freemarkerConfig.getTemplate(templateName);
            stringWriter = new StringWriter();
            template.process(dataModel, stringWriter);
        } catch (Exception e) {
            log.error("Unable to fetch template {}", templateName, e);
        }
        return stringWriter;
    }

    private static String retrieveMail(UserResource user, UserInstitution institution) {
        return Optional.ofNullable(user.getWorkContacts())
                .map(contacts -> contacts.getOrDefault(institution.getUserMailUuid(), null))
                .map(WorkContactResource::getEmail)
                .map(CertifiableFieldResourceOfstring::getValue)
                .filter(StringUtils::isNotBlank)
                .orElseThrow(() -> new InvalidRequestException("Missing mail for userId: " + user.getId()));
    }
}