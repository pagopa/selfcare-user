package it.pagopa.selfcare.user.util;

import io.smallrye.mutiny.Uni;
import it.pagopa.selfcare.product.service.ProductService;
import it.pagopa.selfcare.user.common.PartyRole;
import it.pagopa.selfcare.user.constant.OnboardedProductState;
import it.pagopa.selfcare.user.entity.UserInstitution;
import it.pagopa.selfcare.user.exception.InvalidRequestException;
import it.pagopa.selfcare.user.mapper.NotificationMapper;
import it.pagopa.selfcare.user.mapper.UserNotificationMapper;
import it.pagopa.selfcare.user.model.notification.UserNotificationToSend;
import it.pagopa.selfcare.user.model.notification.UserToNotify;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import org.gradle.internal.impldep.org.apache.commons.lang.StringUtils;
import org.openapi.quarkus.user_registry_json.model.UserResource;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static it.pagopa.selfcare.user.constant.CustomError.ROLE_IS_NULL;
import static it.pagopa.selfcare.user.constant.CustomError.ROLE_NOT_FOUND;

@ApplicationScoped
@RequiredArgsConstructor
public class UserUtils {

    private ProductService productService;
    private UserNotificationMapper userNotificationMapper;
    private NotificationMapper notificationMapper;
    /**
     * The checkRoles function is used to check if the product role provided by the user
     * matches one of the roles defined in a product. If it does not, an exception is thrown.
     */
    public Uni<Boolean> checkRoles(String productId, PartyRole role, String productRole) {
        if (StringUtils.isNotBlank(productRole)) {
            if (role == null) {
                throw new InvalidRequestException(ROLE_IS_NULL.getMessage(), ROLE_IS_NULL.getCode());
            }
            return Uni.createFrom().item(productService.getProduct(productId))
                    .log()
                    .map(product -> product.getRoleMappings().get(role))
                    .map(productRoleInfo -> {
                        if (productRoleInfo == null) {
                            throw new InvalidRequestException(ROLE_NOT_FOUND.getMessage(), ROLE_NOT_FOUND.getCode());
                        }
                        return productRoleInfo.getRoles().stream().anyMatch(prodRole -> prodRole.getCode().equals(productRole));
                    });
        }
        return Uni.createFrom().item(true);
    }

    public List<UserNotificationToSend> constructUserNotificationToSend(UserInstitution userInstitution, UserResource userResource, String productId) {
        return userInstitution.getProducts().stream()
                .map(onboardedProduct -> {
                    if (!StringUtils.isNotBlank(productId) || (StringUtils.isNotBlank(productId) && productId.equals(onboardedProduct.getProductId()))) {
                        UserToNotify userToNotify = new UserToNotify();
                        //UserToNotify userToNotify = userNotificationMapper.toUserNotify(userResource, onboardedProduct, userInstitution.getInstitutionId());
                        return notificationMapper.setNotificationDetailsFromOnboardedProduct(userToNotify, onboardedProduct, userInstitution.getInstitutionId());
                    }
                    return null;
                })
                .toList();
    }

    /**
     * The filterProduct function takes in a UserInstitution object and an array of states.
     * It then creates a list of OnboardedProductState objects from the array of strings, if the array is not null.
     * If it is null, it sets relationshipStates to be null as well.
     * Then, for each product in userInstitution's products list:
     * if relationshipStates is not null and does not contain that product's status (which should be an OnboardedProductState),
     * remove that product from the list.
     */
    public UserInstitution filterProduct(UserInstitution userInstitution, String[] states) {
        List<OnboardedProductState> relationshipStates = Optional.ofNullable(states)
                .map(this::convertStatesToRelationshipsState)
                .orElse(null);

        userInstitution.getProducts().removeIf(onboardedProduct -> !Objects.isNull(relationshipStates) && !relationshipStates.contains(onboardedProduct.getStatus()));
        return userInstitution;
    }

    public List<OnboardedProductState> convertStatesToRelationshipsState(String[] states) {
        return Arrays.stream(states)
                .map(OnboardedProductState::valueOf)
                .collect(Collectors.toList());
    }
}
