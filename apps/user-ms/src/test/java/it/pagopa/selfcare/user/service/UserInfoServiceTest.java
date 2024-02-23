package it.pagopa.selfcare.user.service;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheQuery;
import io.quarkus.panache.mock.PanacheMock;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.mongodb.MongoTestResource;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import it.pagopa.selfcare.user.controller.response.UserInfoResponse;
import it.pagopa.selfcare.user.entity.UserInfo;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openapi.quarkus.user_registry_json.model.UserResource;
import org.openapi.quarkus.user_registry_json.model.WorkContactResource;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@QuarkusTest
@QuarkusTestResource(MongoTestResource.class)
class UserInfoServiceTest {

    @Inject
    private UserInfoServiceDefault userInfoService;

    @Inject
    UserRegistryService userRegistryService;

    @InjectMock
    private UserService userService;

    @RestClient
    @InjectMock
    private org.openapi.quarkus.user_registry_json.api.UserApi userRegistryApi;

    private static UserResource userResource;
    private static final String USERS_FIELD_LIST_WITHOUT_FISCAL_CODE = "name,familyName,email,workContacts";

    static {
        userResource = new org.openapi.quarkus.user_registry_json.model.UserResource();
        userResource.setId(UUID.randomUUID());
        org.openapi.quarkus.user_registry_json.model.CertifiableFieldResourceOfstring certifiedName = new org.openapi.quarkus.user_registry_json.model.CertifiableFieldResourceOfstring();
        certifiedName.setValue("name");
        userResource.setName(certifiedName);
        userResource.setFamilyName(certifiedName);
        userResource.setFiscalCode("taxCode");
        org.openapi.quarkus.user_registry_json.model.CertifiableFieldResourceOfstring certifiedEmail = new org.openapi.quarkus.user_registry_json.model.CertifiableFieldResourceOfstring();
        certifiedEmail.setValue("test@test.it");
        org.openapi.quarkus.user_registry_json.model.WorkContactResource workContactResource = new org.openapi.quarkus.user_registry_json.model.WorkContactResource();
        workContactResource.setEmail(certifiedEmail);
        userResource.setEmail(certifiedEmail);
        Map<String, WorkContactResource> workContactsMap = new HashMap<>();
        workContactsMap.put("institutionId", workContactResource);
        userResource.setWorkContacts(workContactsMap);
    }

    @Test
    void findByUserId() {
        final String userId = "userId";
        UserInfo userInfo = createDummyUserInfo();
        PanacheMock.mock(UserInfo.class);
        ReactivePanacheQuery query = Mockito.mock(ReactivePanacheQuery.class);
        when(query.firstResult()).thenReturn(Uni.createFrom().item(userInfo));
        when(UserInfo.find(any(), (Object) any()))
                .thenReturn(query);
        Uni<UserInfoResponse> response = userInfoService.findById(userId);
        Assertions.assertNotNull(response);
    }

    @Test
    void updateUserEmails() {
        final int size = 1, page = 1;
        var userInfo = createDummyUserInfo();
        PanacheMock.mock(UserInfo.class);
        ReactivePanacheQuery query = Mockito.mock(ReactivePanacheQuery.class);
        when(UserInfo.findAll())
                .thenReturn(query);
        when(query.page(anyInt(), anyInt())).thenReturn(query);
        when(query.stream()).thenReturn(Multi.createFrom().items(userInfo));
        when(userService.updateUserInstitutionEmail(any(), any(), any())).thenReturn(Uni.createFrom().voidItem());
        when(userRegistryApi.updateUsingPATCH(any(), any())).thenReturn(Uni.createFrom().item(Response.accepted().build()));
        when(userRegistryApi.findByIdUsingGET(USERS_FIELD_LIST_WITHOUT_FISCAL_CODE, "userId")).thenReturn(Uni.createFrom().item(userResource));
        UniAssertSubscriber<Void> subscriber = userInfoService.updateUsersEmails(size, page)
                .subscribe().withSubscriber(UniAssertSubscriber.create());
        Assertions.assertNotNull(subscriber.assertCompleted().awaitItem());
    }

    private UserInfo createDummyUserInfo() {
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId("userId");
        return userInfo;
    }

}
