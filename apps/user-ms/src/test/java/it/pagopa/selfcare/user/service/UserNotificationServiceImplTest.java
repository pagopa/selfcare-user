package it.pagopa.selfcare.user.service;

import freemarker.template.Configuration;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import it.pagopa.selfcare.onboarding.common.PartyRole;
import it.pagopa.selfcare.product.entity.Product;
import it.pagopa.selfcare.product.entity.ProductRole;
import it.pagopa.selfcare.product.entity.ProductRoleInfo;
import it.pagopa.selfcare.user.client.eventhub.EventHubRestClient;
import it.pagopa.selfcare.user.constant.OnboardedProductState;
import it.pagopa.selfcare.user.entity.OnboardedProduct;
import it.pagopa.selfcare.user.entity.UserInstitution;
import it.pagopa.selfcare.user.model.notification.UserNotificationToSend;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;
import org.openapi.quarkus.user_registry_json.model.CertifiableFieldResourceOfstring;
import org.openapi.quarkus.user_registry_json.model.UserResource;
import org.openapi.quarkus.user_registry_json.model.WorkContactResource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@QuarkusTest
class UserNotificationServiceImplTest {
    @Inject
    UserNotificationService userNotificationService;

    @InjectMock
    private MailService mailService;

    @RestClient
    @InjectMock
    private EventHubRestClient eventHubRestClient;

    private static final UserResource userResource;
    private static final UserInstitution userInstitution;
    private static final Product product;

    @Produces
    @ApplicationScoped
    Configuration freemarkerConfig() {
        return mock(Configuration.class);
    }

    static {
        userResource = new UserResource();
        userResource.setId(UUID.randomUUID());
        CertifiableFieldResourceOfstring certifiedName = new CertifiableFieldResourceOfstring();
        certifiedName.setValue("name");
        userResource.setName(certifiedName);
        userResource.setFamilyName(certifiedName);
        userResource.setFiscalCode("taxCode");
        CertifiableFieldResourceOfstring certifiedEmail = new CertifiableFieldResourceOfstring();
        certifiedEmail.setValue("test@test.it");
        WorkContactResource workContactResource = new WorkContactResource();
        workContactResource.setEmail(certifiedEmail);
        userResource.setEmail(certifiedEmail);
        userResource.setWorkContacts(Map.of("IdMail", workContactResource));

        userInstitution = new UserInstitution();
        userInstitution.setId(ObjectId.get());
        userInstitution.setUserId("userId");
        userInstitution.setUserMailUuid("IdMail");
        userInstitution.setInstitutionId("institutionId");
        userInstitution.setInstitutionRootName("institutionRootName");
        OnboardedProduct onboardedProduct = new OnboardedProduct();
        onboardedProduct.setProductId("test");
        onboardedProduct.setProductRole("code");
        userInstitution.setProducts(List.of(onboardedProduct));


        ProductRoleInfo productRoleInfo = new ProductRoleInfo();
        var productRole = new ProductRole();
        productRole.setCode("code");
        productRole.setDescription("description");
        productRole.setLabel("label");
        productRoleInfo.setRoles(List.of(productRole));

        product = new Product();
        product.setId("test");
        product.setRoleMappings(new HashMap<>() {{
            put(PartyRole.MANAGER, productRoleInfo);
        }});
    }

    @Test
    void testSendMailNotification() {
        String loggedUserName = "loggedUserName";
        String loggedUserSurname = "loggedUserSurname";

        when(mailService.sendMail(anyString(), anyString(), anyString())).thenReturn(Uni.createFrom().voidItem());

        userNotificationService.sendEmailNotification(
                        userResource,
                        userInstitution,
                        product,
                        OnboardedProductState.ACTIVE,
                        loggedUserName,
                        loggedUserSurname
                )
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertCompleted();
        userNotificationService.sendEmailNotification(
                        userResource,
                        userInstitution,
                        product,
                        OnboardedProductState.DELETED,
                        loggedUserName,
                        loggedUserSurname
                )
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertCompleted();
        userNotificationService.sendEmailNotification(
                        userResource,
                        userInstitution,
                        product,
                        OnboardedProductState.SUSPENDED,
                        loggedUserName,
                        loggedUserSurname
                )
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertCompleted();
        userNotificationService.sendEmailNotification(
                        userResource,
                        userInstitution,
                        product,
                        OnboardedProductState.PENDING,
                        loggedUserName,
                        loggedUserSurname
                )
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertCompleted();
        verify(mailService, times(3)).sendMail(anyString(), anyString(), anyString());
    }

    @Test
    void testSendKafkaNotification(){
        UserNotificationToSend userNotificationToSend = new UserNotificationToSend();
        userNotificationToSend.setId("userId");

        when(eventHubRestClient.sendMessage(any())).thenReturn(Uni.createFrom().voidItem());

        UniAssertSubscriber<UserNotificationToSend> subscriber = userNotificationService.sendKafkaNotification(
                userNotificationToSend,
                "userId"
        ).subscribe().withSubscriber(UniAssertSubscriber.create());
        subscriber.assertCompleted();
        verify(eventHubRestClient, times(1)).sendMessage(userNotificationToSend);

    }
}
