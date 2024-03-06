package it.pagopa.selfcare.user.controller;


import it.pagopa.selfcare.user.model.notification.UserNotificationToSend;
import it.pagopa.selfcare.user.service.EventhubService;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import lombok.AllArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.time.LocalDateTime;

@Path("/eventhub")
@AllArgsConstructor
public class EventHubController {

    private final EventhubService eventhubService;

    @Operation(summary = "Test EventHub Api")
    @POST
    @Path(value = "/")
    public void sendMessage(){
        UserNotificationToSend notificationToSend = new UserNotificationToSend();
        notificationToSend.setId("TestMessageId");
        notificationToSend.setCreatedAt(LocalDateTime.now());
        notificationToSend.setProductId("EventHubTest");
        eventhubService.sendNotification(notificationToSend.toString());
    }
}
