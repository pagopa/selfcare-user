package it.pagopa.selfcare.user.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.smallrye.reactive.messaging.MutinyEmitter;
import it.pagopa.selfcare.product.entity.Product;
import it.pagopa.selfcare.product.entity.ProductRole;
import it.pagopa.selfcare.user.conf.CloudTemplateLoader;
import it.pagopa.selfcare.user.constant.OnboardedProductState;
import it.pagopa.selfcare.user.entity.OnboardedProduct;
import it.pagopa.selfcare.user.entity.UserInstitution;
import it.pagopa.selfcare.user.exception.InvalidRequestException;
import it.pagopa.selfcare.user.model.notification.UserNotificationToSend;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.openapi.quarkus.notification_manager_json.api.NotificationManagerApi;
import org.openapi.quarkus.notification_manager_json.model.CreateMessageToUserDto;
import org.openapi.quarkus.user_registry_json.model.UserResource;
import org.openapi.quarkus.user_registry_json.model.WorkContactResource;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static it.pagopa.selfcare.user.constant.TemplateMailConstant.*;


@Slf4j
@ApplicationScoped
public class UserNotificationServiceImpl implements UserNotificationService {


    public static final String ERROR_DURING_SEND_DATA_LAKE_NOTIFICATION_FOR_USER = "error during send dataLake notification for user {}";

    @RestClient
    @Inject
    NotificationManagerApi notificationManagerApi;
    @Inject
    @Channel("sc-users")
    MutinyEmitter<String> usersEmitter;

    private final Configuration freemarkerConfig;

    private final ObjectMapper objectMapper;


    public UserNotificationServiceImpl(Configuration freemarkerConfig, CloudTemplateLoader cloudTemplateLoader, ObjectMapper objectMapper) {
        this.freemarkerConfig = freemarkerConfig;
        this.freemarkerConfig.setTemplateLoader(cloudTemplateLoader);
        this.objectMapper = objectMapper;
    }

    private String convertNotificationToJson(UserNotificationToSend userNotificationToSend) {
        try {
            return objectMapper.writeValueAsString(userNotificationToSend);
        } catch (JsonProcessingException e) {
            log.warn(ERROR_DURING_SEND_DATA_LAKE_NOTIFICATION_FOR_USER, userNotificationToSend.getUser().getUserId());
            throw new InvalidRequestException(ERROR_DURING_SEND_DATA_LAKE_NOTIFICATION_FOR_USER);
        }
    }

    @Override
    public Uni<UserNotificationToSend> sendKafkaNotification(UserNotificationToSend userNotificationToSend, String userId) {
        String message = convertNotificationToJson(userNotificationToSend);
        return usersEmitter.sendMessage(Message.of(message))
                .onItem().invoke(() -> log.info("sent dataLake notification for user : {}", userId))
                .onFailure().invoke(throwable -> log.warn("error during send dataLake notification for user {}: {} ", userId, throwable.getMessage(), throwable))
                .replaceWith(userNotificationToSend);
    }

    @Override
    public Uni<Void> sendEmailNotification(UserResource user, UserInstitution institution, Product product, OnboardedProductState status, String loggedUserName, String loggedUserSurname) {
        log.info("sendMailNotification {}", status.name());
        return switch (status) {
            case ACTIVE ->
                    this.buildAndSendEmailNotification(user, institution, product, ACTIVATE_TEMPLATE, ACTIVATE_SUBJECT, loggedUserName, loggedUserSurname);
            case SUSPENDED ->
                    this.buildAndSendEmailNotification(user, institution, product, DELETE_TEMPLATE, DELETE_SUBJECT, loggedUserName, loggedUserSurname);
            case DELETED ->
                    this.buildAndSendEmailNotification(user, institution, product, SUSPEND_TEMPLATE, SUSPEND_SUBJECT, loggedUserName, loggedUserSurname);
            case PENDING, TOBEVALIDATED, REJECTED -> Uni.createFrom().voidItem();
        };
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

    private Uni<Void> buildAndSendEmailNotification(UserResource user, UserInstitution institution, Product product, String templateName, String subject, String loggedUserName, String loggedUserSurname) {
        return Uni.createFrom().voidItem().onItem().transformToUni(Unchecked.function(x -> {

            WorkContactResource certEmail = user.getWorkContacts().get(institution.getUserMailUuid());

            String email;
            if (certEmail == null || certEmail.getEmail() == null || StringUtils.isBlank(certEmail.getEmail().getValue())) {
                throw new InvalidRequestException("Missing mail for userId: " + user.getId());
            } else {
                email = certEmail.getEmail().getValue();
            }

            Map<String, String> dataModel = buildEmailDataModel(institution, product, loggedUserName, loggedUserSurname);

            return sendNotification(email, templateName, subject, dataModel);
        }));
    }

    private Uni<Void> sendNotification(String email, String templateName, String subject, Map<String, String> dataModel) {
        return Uni.createFrom().item(getStringWriter(templateName, dataModel))
                .onItem().transform(Unchecked.function(template -> {
                    CreateMessageToUserDto messageRequest = new CreateMessageToUserDto();
                    messageRequest.setContent(template.toString());
                    messageRequest.setReceiverEmail(email);
                    messageRequest.setSubject(subject);
                    return messageRequest;
                }))
                .onItem().transformToUni(messageRequest ->
                        notificationManagerApi.sendNotificationToUserUsingPOST(messageRequest))
                .onFailure().invoke(throwable -> log.error("Error during send mail to: {} -> exception: {}",email,throwable.getMessage(), throwable))
                .replaceWithVoid();
    }

    private StringWriter getStringWriter(String templateName, Map<String, String> dataModel) {
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
}
