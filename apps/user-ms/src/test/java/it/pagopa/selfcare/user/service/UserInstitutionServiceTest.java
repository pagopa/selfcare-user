package it.pagopa.selfcare.user.service;

import io.quarkus.mongodb.panache.common.reactive.ReactivePanacheUpdate;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheQuery;
import io.quarkus.panache.mock.PanacheMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.mongodb.MongoTestResource;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.AssertSubscriber;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import it.pagopa.selfcare.user.constant.OnboardedProductState;
import it.pagopa.selfcare.user.controller.response.UserInstitutionResponse;
import it.pagopa.selfcare.user.entity.UserInstitution;
import jakarta.inject.Inject;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.smallrye.common.constraint.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@QuarkusTest
@QuarkusTestResource(MongoTestResource.class)
class UserInstitutionServiceTest {

    @Inject
    private UserInstitutionServiceDefault userInstitutionService;

    @Test
    void findById() {
        final String id = new ObjectId().toHexString();
        UserInstitution userInstitution = createDummyUserInstitution();
        PanacheMock.mock(UserInstitution.class);
        when(UserInstitution.findById(any()))
                .thenReturn(Uni.createFrom().item(userInstitution));
        Uni<UserInstitutionResponse> response = userInstitutionService.findById(id);
        Assertions.assertNotNull(response);
    }

    @Test
    void findByInstitutionId() {
        final String institutionId = "institutionId";
        UserInstitution userInstitution = createDummyUserInstitution();
        PanacheMock.mock(UserInstitution.class);
        ReactivePanacheQuery query = Mockito.mock(ReactivePanacheQuery.class);
        when(query.firstResult()).thenReturn(Uni.createFrom().item(userInstitution));
        when(UserInstitution.find(any(), (Object) any()))
                .thenReturn(query);
        Uni<UserInstitutionResponse> response = userInstitutionService.findByInstitutionId(institutionId);
        Assertions.assertNotNull(response);
    }

    @Test
    void findByUserId() {
        final String userId = "userId";
        UserInstitution userInstitution = createDummyUserInstitution();
        PanacheMock.mock(UserInstitution.class);
        ReactivePanacheQuery query = Mockito.mock(ReactivePanacheQuery.class);
        when(query.firstResult()).thenReturn(Uni.createFrom().item(userInstitution));
        when(UserInstitution.find(any(), (Object) any()))
                .thenReturn(query);
        Multi<UserInstitutionResponse> response = userInstitutionService.findByUserId(userId);
        Assertions.assertNotNull(response);
    }

    @Test
    void paginatedFindAllWithFilter() {
        Map<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("institutionId", "institutionId");
        UserInstitution userInstitution = createDummyUserInstitution();
        PanacheMock.mock(UserInstitution.class);
        ReactivePanacheQuery query = Mockito.mock(ReactivePanacheQuery.class);
        when(query.firstResult()).thenReturn(Uni.createFrom().item(userInstitution));
        when(UserInstitution.find((Document) any(), any()))
                .thenReturn(query);
        when(query.page(anyInt(), anyInt())).thenReturn(query);
        when(query.stream()).thenReturn(Multi.createFrom().item(userInstitution));
        AssertSubscriber<UserInstitution> subscriber =  userInstitutionService.paginatedFindAllWithFilter(parameterMap, 0, 100)
                .subscribe().withSubscriber(AssertSubscriber.create(10));
        List<UserInstitution> response = subscriber.assertCompleted().getItems();
        Assertions.assertEquals(1, response.size());
    }


    @Test
    void retrieveFirstFilteredUserInstitution() {
        Map<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("institutionId", "institutionId");
        UserInstitution userInstitution = createDummyUserInstitution();
        PanacheMock.mock(UserInstitution.class);
        ReactivePanacheQuery query = Mockito.mock(ReactivePanacheQuery.class);
        when(query.firstResult()).thenReturn(Uni.createFrom().item(userInstitution));
        when(UserInstitution.find((Document) any(), any()))
                .thenReturn(query);
        when(query.page(anyInt(), anyInt())).thenReturn(query);
        when(query.firstResult()).thenReturn(Uni.createFrom().item(userInstitution));
        UniAssertSubscriber<UserInstitution> subscriber =  userInstitutionService.retrieveFirstFilteredUserInstitution(parameterMap)
                .subscribe().withSubscriber(UniAssertSubscriber.create());
        subscriber.assertCompleted().assertItem(userInstitution);
    }

    @Test
    void findAllWithFilter() {
        Map<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("institutionId", "institutionId");
        UserInstitution userInstitution = createDummyUserInstitution();
        PanacheMock.mock(UserInstitution.class);
        ReactivePanacheQuery query = Mockito.mock(ReactivePanacheQuery.class);
        when(query.firstResult()).thenReturn(Uni.createFrom().item(userInstitution));
        when(UserInstitution.find(any(Document.class), eq(null)))
                .thenReturn(query);
        when(query.page(anyInt(), anyInt())).thenReturn(query);
        when(query.stream()).thenReturn(Multi.createFrom().item(userInstitution));

        AssertSubscriber<UserInstitution> subscriber =  userInstitutionService.findAllWithFilter(parameterMap)
                .subscribe().withSubscriber(AssertSubscriber.create(10));

        List<UserInstitution> actual = subscriber.assertCompleted().getItems();
        assertNotNull(actual);
        assertEquals(1, actual.size());
    }

    @Test
    void retrieveFilteredUserInstitution() {
        Map<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("institutionId", "institutionId");
        List<UserInstitution> userInstitutionList = new ArrayList<>();
        UserInstitution userInstitution = createDummyUserInstitution();
        userInstitutionList.add(userInstitution);
        PanacheMock.mock(UserInstitution.class);
        ReactivePanacheQuery query = Mockito.mock(ReactivePanacheQuery.class);
        when(query.firstResult()).thenReturn(Uni.createFrom().item(userInstitutionList));
        when(UserInstitution.find((Document) any(), any()))
                .thenReturn(query);
        when(query.page(anyInt(), anyInt())).thenReturn(query);
        when(query.list()).thenReturn(Uni.createFrom().item(userInstitutionList));
        UniAssertSubscriber<List<UserInstitution>> subscriber =  userInstitutionService.retrieveFilteredUserInstitution(parameterMap)
                .subscribe().withSubscriber(UniAssertSubscriber.create());
        subscriber.assertCompleted().assertItem(userInstitutionList);
    }

    @Test
    void updateUserStatusWithInstitutionAndOnboardedFilter() {
        final String userId = "userId";
        String institutionId = "institutionId";
        String productId = "productId";
        PanacheMock.mock(UserInstitution.class);
        ReactivePanacheUpdate update = Mockito.mock(ReactivePanacheUpdate.class);
        when(UserInstitution.update(any(Document.class)))
                .thenReturn(update);
        when(update.where(any())).thenReturn(Uni.createFrom().item(1L));
        UniAssertSubscriber<Long> subscriber = userInstitutionService.updateUserStatusWithOptionalFilterByInstitutionAndProduct(userId, institutionId, productId, null, null, OnboardedProductState.ACTIVE)
                .subscribe().withSubscriber(UniAssertSubscriber.create());
        subscriber.assertCompleted().assertItem(1L);
    }

    @Test
    void updateUserStatusWithInstitutionFilter() {
        final String userId = "userId";
        String institutionId = "institutionId";
        PanacheMock.mock(UserInstitution.class);
        ReactivePanacheUpdate update = Mockito.mock(ReactivePanacheUpdate.class);
        when(UserInstitution.update(any(Document.class)))
                .thenReturn(update);
        when(update.where(any())).thenReturn(Uni.createFrom().item(1L));
        UniAssertSubscriber<Long> subscriber = userInstitutionService.updateUserStatusWithOptionalFilterByInstitutionAndProduct(userId, institutionId, null, null, null, OnboardedProductState.ACTIVE)
                .subscribe().withSubscriber(UniAssertSubscriber.create());
        subscriber.assertCompleted().assertItem(1L);
    }

    @Test
    void deleteUserInstitutionProduct(){
        final String userId = "userId";
        String institutionId = "institutionId";
        PanacheMock.mock(UserInstitution.class);
        ReactivePanacheUpdate update = Mockito.mock(ReactivePanacheUpdate.class);
        when(UserInstitution.update(any(Document.class)))
                .thenReturn(update);
        when(update.where(any())).thenReturn(Uni.createFrom().item(1L));
        UniAssertSubscriber<Long> subscriber = userInstitutionService.deleteUserInstitutionProduct(userId, institutionId, "productID")
                .subscribe().withSubscriber(UniAssertSubscriber.create());
        subscriber.assertCompleted().assertItem(1L);
    }

    @Test
    void updateUserProductCreatedAt() {
        final String userId = "userId";
        final String institutionId = "institutionId";
        final String productId = "productId";
        PanacheMock.mock(UserInstitution.class);
        ReactivePanacheUpdate update = Mockito.mock(ReactivePanacheUpdate.class);
        when(UserInstitution.update(any(Document.class)))
                .thenReturn(update);
        when(update.where(any())).thenReturn(Uni.createFrom().item(1L));
        UniAssertSubscriber<Long> subscriber = userInstitutionService.updateUserCreatedAtByInstitutionAndProduct(institutionId, List.of(userId), productId, LocalDateTime.now())
                .subscribe().withSubscriber(UniAssertSubscriber.create());
        subscriber.assertCompleted().assertItem(1L);
    }

    @Test
    void findByUserIdAndInstitutionId() {
        final String userId = "userId";
        final String institutionId = "institutionId";
        UserInstitution userInstitution = createDummyUserInstitution();
        PanacheMock.mock(UserInstitution.class);
        ReactivePanacheQuery query = Mockito.mock(ReactivePanacheQuery.class);
        when(query.firstResult()).thenReturn(Uni.createFrom().item(userInstitution));
        when(UserInstitution.find(any(Document.class), eq(null)))
                .thenReturn(query);
        UniAssertSubscriber<UserInstitution> subscriber = userInstitutionService.findByUserIdAndInstitutionId(userId, institutionId)
                .subscribe().withSubscriber(UniAssertSubscriber.create());
        UserInstitution actual = subscriber.assertCompleted().awaitItem().getItem();
        Assertions.assertNotNull(actual);
    }

    @Test
    void findByUserIdAndInstitutionId_WithNoResult() {
        final String userId = "userId";
        final String institutionId = "institutionId";
        PanacheMock.mock(UserInstitution.class);
        ReactivePanacheQuery query = Mockito.mock(ReactivePanacheQuery.class);
        when(query.firstResult()).thenReturn(Uni.createFrom().nullItem());
        when(UserInstitution.find(any(Document.class), eq(null)))
                .thenReturn(query);
        UniAssertSubscriber<UserInstitution> subscriber = userInstitutionService.findByUserIdAndInstitutionId(userId, institutionId)
                .subscribe().withSubscriber(UniAssertSubscriber.create());
        UserInstitution actual = subscriber.assertCompleted().awaitItem().getItem();
        Assertions.assertNull(actual);
    }

    private UserInstitution createDummyUserInstitution() {
        UserInstitution userInstitution = new UserInstitution();
        userInstitution.setId(ObjectId.get());
        userInstitution.setUserId("userId");
        userInstitution.setInstitutionId("institutionId");
        userInstitution.setInstitutionRootName("institutionRootName");
        return userInstitution;
    }


}
