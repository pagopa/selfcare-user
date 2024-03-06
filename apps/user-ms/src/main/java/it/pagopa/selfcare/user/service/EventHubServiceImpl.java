package it.pagopa.selfcare.user.service;

import it.pagopa.selfcare.user.client.eventhub.EventHubRestClient;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Slf4j
@ApplicationScoped
public class EventHubServiceImpl implements EventhubService{

    @RestClient
//    @Inject
    private EventHubRestClient restClient;

    @Override
    public void sendNotification(String notification) {
        log.trace("method accessed");
        restClient.sendMessage(notification);
    }
}
