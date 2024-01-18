package it.pagopa.selfcare.user.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import it.pagopa.selfcare.user.constant.QueueEvent;
import it.pagopa.selfcare.user.entity.OnboardedProduct;
import it.pagopa.selfcare.user.entity.UserInstitution;
import it.pagopa.selfcare.user.exception.InvalidRequestException;
import it.pagopa.selfcare.user.model.notification.UserNotificationToSend;
import it.pagopa.selfcare.user.model.notification.UserToNotify;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.openapi.quarkus.user_registry_json.api.UserApi;
import org.openapi.quarkus.user_registry_json.model.UserResource;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class UserEventServiceImpl implements UserEventService {

    public static final String ERROR_DURING_SEND_DATA_LAKE_NOTIFICATION_FOR_USER = "error during send dataLake notification for user {}";
    public static final String USERS_FIELD_LIST = "fiscalCode,name,familyName,email,workContacts";
    public static final String USERS_FIELD_LIST_WITHOUT_FISCAL_CODE = "name,familyName,email,workContacts";


    private final ObjectMapper objectMapper;
    private final UserApi userApi;

    @Channel("sc-users")
    private final Emitter<String> usersEmitter;

    @Override
    public Uni<Void> sendUpdateUserNotificationToQueue(String userId, String institutionId) {
        log.trace("sendUpdateUserNotification start");
        log.debug("sendUpdateUserNotification userId = {}, institutionId = {}", userId, institutionId);
        return Uni.createFrom().item(constructUserNotificationToSend(userId, institutionId))
                .map(this::retrievemessage)
                .flatMap(notification -> sendUserNotification(notification, userId));
    }

    @Override
    public Uni<Void> sendOperatorUserNotification(UserInstitution userInstitution, String relationshipId, QueueEvent eventType) {
        return Uni.combine().all().unis(userApi.findByIdUsingGET(USERS_FIELD_LIST_WITHOUT_FISCAL_CODE, userInstitution.getUserId()),
                        Uni.createFrom().item(userInstitution))
                .asTuple()
                .map(tuple -> constructUserNotificationToSend(tuple.getItem2(), relationshipId, tuple.getItem1(), eventType))
                .map(this::retrievemessage)
                .flatMap(notification -> sendUserNotification(notification, userInstitution.getUserId()));

    }

    private UserNotificationToSend addId(UserNotificationToSend userNotificationToSend, String userId, String productRole) {
        String id = idBuilder(userId, userNotificationToSend.getInstitutionId(), userNotificationToSend.getProductId(), productRole);
        userNotificationToSend.setId(id);
        return userNotificationToSend;
    }

    private String idBuilder(String userId, String institutionId, String productId, String productRole) {
        return String.format("%s_%s_%s_%s", userId, institutionId, productId, productRole);
    }

    private UserToNotify toUserToNotify(String userId, String institutionId, UserResource user, OnboardedProduct onboardedProduct) {
        UserToNotify userToNotify = new UserToNotify();
        userToNotify.setUserId(userId);
        userToNotify.setName(user.getName().getValue());
        userToNotify.setFamilyName(user.getFamilyName().getValue());
        userToNotify.setEmail(user.getWorkContacts().containsKey(institutionId) ? user.getWorkContacts().get(institutionId).getEmail().getValue() : user.getEmail().getValue());
        userToNotify.setRole(onboardedProduct.getRole());
        userToNotify.setRelationshipStatus(onboardedProduct.getStatus());
        userToNotify.setProductRole(onboardedProduct.getProductRole());
        return userToNotify;
    }

    private String retrievemessage(UserNotificationToSend notification) {
        try {
            return objectMapper.writeValueAsString(notification);
        } catch (JsonProcessingException e) {
            log.warn(ERROR_DURING_SEND_DATA_LAKE_NOTIFICATION_FOR_USER, notification.getUser().getUserId());
            throw new InvalidRequestException(ERROR_DURING_SEND_DATA_LAKE_NOTIFICATION_FOR_USER);
        }
    }

    private UserNotificationToSend constructUserNotificationToSend(String userId, String institutionId) {
        UserToNotify userToNotify = new UserToNotify();
        userToNotify.setUserId(userId);
        UserNotificationToSend notification = new UserNotificationToSend();
        String id = userToNotify.getUserId().concat(institutionId);
        notification.setId(id);
        notification.setUpdatedAt(OffsetDateTime.now());
        notification.setInstitutionId(institutionId);
        notification.setEventType(QueueEvent.UPDATE);
        notification.setUser(userToNotify);
        return notification;
    }

    private UserNotificationToSend constructUserNotificationToSend(UserInstitution userInstitution, String relationshipId, UserResource userResource, QueueEvent eventType) {
        UserNotificationToSend notification = new UserNotificationToSend();

        Optional<OnboardedProduct> onboardedProductOpt = userInstitution.getProducts().stream()
                .filter(prod -> relationshipId.equalsIgnoreCase(prod.getRelationshipId())).findFirst();

        if(onboardedProductOpt.isPresent()) {
            notification.setUpdatedAt(OffsetDateTime.now());
            notification.setInstitutionId(userInstitution.getInstitutionId());
            notification.setProductId(onboardedProductOpt.get().getProductId());
            notification.setOnboardingTokenId(onboardedProductOpt.get().getTokenId());
            notification.setEventType(eventType);

            UserToNotify userToNotify = toUserToNotify(userInstitution.getUserId(), userInstitution.getInstitutionId(), userResource, onboardedProductOpt.get());
            notification.setUser(userToNotify);

            addId(notification, userInstitution.getUserId(), onboardedProductOpt.get().getProductRole());
        }
        return notification;
    }

    private Uni<Void> sendUserNotification(String message, String userId) {
        usersEmitter.send(Message.of(message)
                .withAck(() -> {
                    log.info("sent dataLake notification for user : {}", userId);
                    return CompletableFuture.completedFuture(null);
                })
                .withNack(throwable -> {
                    log.warn("error during send dataLake notification for user {}: {} ", userId, throwable.getMessage(), throwable);
                    return CompletableFuture.completedFuture(null);
                }));

        return Uni.createFrom().voidItem();

    }
}
