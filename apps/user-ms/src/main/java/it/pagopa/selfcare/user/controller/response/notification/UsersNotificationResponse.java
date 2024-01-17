package it.pagopa.selfcare.user.controller.response.notification;

import it.pagopa.selfcare.user.controller.response.UserNotificationResponse;
import lombok.Data;

import java.util.List;

@Data
public class UsersNotificationResponse {

    private List<UserNotificationResponse> users;
}
