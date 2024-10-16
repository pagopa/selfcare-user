package it.pagopa.selfcare.user.event.service;

import com.mongodb.client.model.changestream.ChangeStreamDocument;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.mongodb.MongoTestResource;
import it.pagopa.selfcare.user.client.EventHubRestClient;
import it.pagopa.selfcare.user.event.UserGroupCdcService;
import it.pagopa.selfcare.user.event.entity.UserGroupEntity;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

@QuarkusTest
@QuarkusTestResource(MongoTestResource.class)
public class UserGroupCdcServiceTest {

    @Inject
    UserGroupCdcService userGroupCdcService;

    @RestClient
    @InjectMock
    EventHubRestClient eventHubRestClient;

    @Test
    void consumerToSendScUserGroupEventTest() {
        UserGroupEntity userGroupEntity = dummyUserGroup();
        ChangeStreamDocument<UserGroupEntity> document = Mockito.mock(ChangeStreamDocument.class);
        when(document.getFullDocument()).thenReturn(userGroupEntity);
        userGroupCdcService.consumerToSendScUserGroupEvent(document);
        verify(eventHubRestClient, times(1)).
                sendMessage(any());

    }

    UserGroupEntity dummyUserGroup() {
        UserGroupEntity userGroupEntity = new UserGroupEntity();
        userGroupEntity.setId("UserGroupId");
        return userGroupEntity;
    }
}
