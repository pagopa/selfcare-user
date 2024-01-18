package it.pagopa.selfcare.user.controller.response.notification;

import it.pagopa.selfcare.user.constant.QueueEvent;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * This objects wrap user's info sent on topic sc-users
 */
@Data
public class UserNotificationResponse {

    private String id;
    private String institutionId;
    private String productId;
    private String onboardingTokenId;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private QueueEvent eventType;
    private UserToNotify user;

}
