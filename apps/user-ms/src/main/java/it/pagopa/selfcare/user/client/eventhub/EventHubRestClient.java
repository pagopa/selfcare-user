package it.pagopa.selfcare.user.client.eventhub;

import io.smallrye.mutiny.Uni;
import it.pagopa.selfcare.user.client.auth.EventhubSasTokenAuthorization;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;


@Path("/")
@RegisterRestClient(configKey = "event-hub")
//@ApplicationScoped
@RegisterProvider(EventhubSasTokenAuthorization.class)
public interface EventHubRestClient {

    @POST
    @Path(value = "messages")
    Uni<Void> sendMessage(String notification);

}
