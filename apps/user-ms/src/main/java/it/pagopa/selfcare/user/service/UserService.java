package it.pagopa.selfcare.user.service;

import io.smallrye.mutiny.Uni;
import it.pagopa.selfcare.user.common.PartyRole;
import it.pagopa.selfcare.user.constant.OnboardedProductState;
import it.pagopa.selfcare.user.entity.UserInstitution;
import it.pagopa.selfcare.user.model.OnboardingInfo;
import it.pagopa.selfcare.user.model.notification.UserNotificationToSend;
import org.openapi.quarkus.user_registry_json.model.UserResource;

import java.util.List;

public interface UserService {

    Uni<Void> updateUserStatus(String userId, String institutionId, String productId, PartyRole role, String productRole, OnboardedProductState status);

    Uni<List<UserNotificationToSend>> findPaginatedUserNotificationToSend(Integer size, Integer page, String productId);

    Uni<UserResource> retrievePerson(String userId, String productId, String institutionId);

    Uni<UserInstitution> retrieveBindings(String institutionId, String userId, String[] states);

    Uni<List<OnboardingInfo>> getUserInfo(String userId, String institutionId, String[] states);

}
