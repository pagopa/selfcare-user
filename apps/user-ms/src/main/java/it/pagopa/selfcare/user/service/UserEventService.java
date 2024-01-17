package it.pagopa.selfcare.user.service;


import io.smallrye.mutiny.Uni;
import it.pagopa.selfcare.user.constant.QueueEvent;
import it.pagopa.selfcare.user.entity.UserInstitution;

public interface UserEventService {

    Uni<Void> sendUpdateUserNotificationToQueue(String userId, String institutionId);

    Uni<Void> sendOperatorUserNotification(UserInstitution userInstitution, String relationshipId, QueueEvent eventType);
}
