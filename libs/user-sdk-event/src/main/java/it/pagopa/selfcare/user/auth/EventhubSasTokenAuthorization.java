package it.pagopa.selfcare.user.auth;

import jakarta.inject.Inject;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.net.URI;

import static it.pagopa.selfcare.user.UserUtils.getSASToken;

public class EventhubSasTokenAuthorization implements ClientRequestFilter {

    @Inject
    @ConfigProperty(name = "quarkus.rest-client.event-hub.url")
    URI resourceUri;

    @Inject
    @ConfigProperty(name = "eventhub.rest-client.keyName")
    String keyName;

    @Inject
    @ConfigProperty(name = "eventhub.rest-client.key")
    String key;

    @Override
    public void filter(ClientRequestContext clientRequestContext) throws IOException {
        clientRequestContext.getHeaders().add("Authorization", getSASToken(resourceUri.toString(), keyName, key));

    }
}
