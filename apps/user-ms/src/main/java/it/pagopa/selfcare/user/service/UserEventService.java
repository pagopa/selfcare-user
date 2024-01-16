package it.pagopa.selfcare.user.service;


import io.smallrye.mutiny.Uni;

public interface UserEventService {

    Uni<Boolean> sendUpdateUserNotificationToQueue(String userId, String institutionId);

}
