package it.pagopa.selfcare.user.service;

import io.smallrye.mutiny.Uni;
import it.pagopa.selfcare.user.client.EventHubRestClient;
import it.pagopa.selfcare.user.model.UserNotificationToSend;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

public class EventhubServiceImpl implements EventhubService {

    @Inject
    @RestClient
    EventHubRestClient restClient;

    @Override
    public Uni<Void> sendMessage(UserNotificationToSend notification) {
        return restClient.sendMessage(notification);
    }

}
