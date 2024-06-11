package it.pagopa.selfcare.user.service;

import io.smallrye.mutiny.Uni;
import it.pagopa.selfcare.user.model.UserNotificationToSend;

public interface EventhubService {

    Uni<Void> sendMessage(UserNotificationToSend message);
}
