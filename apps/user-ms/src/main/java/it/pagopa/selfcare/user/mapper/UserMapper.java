package it.pagopa.selfcare.user.mapper;

import it.pagopa.selfcare.user.controller.response.OnboardingInfoResponse;
import it.pagopa.selfcare.user.controller.response.product.UserProductsResponse;
import it.pagopa.selfcare.user.controller.response.UserResponse;
import it.pagopa.selfcare.user.controller.response.notification.UsersNotificationResponse;
import it.pagopa.selfcare.user.entity.OnboardedProduct;
import it.pagopa.selfcare.user.entity.UserInstitution;
import it.pagopa.selfcare.user.model.OnboardingInfo;
import it.pagopa.selfcare.user.model.UserProduct;
import it.pagopa.selfcare.user.model.notification.UserNotificationToSend;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "cdi")
public interface UserMapper {
    //UserResponse toUserResponse(UserResource userResource);

    //OnboardingInfoResponse toUserInfoReponse(String userId, List<OnboardingInfo> onboardingInfos);

  //  UserProductsResponse toUserProductsResponse(UserInstitution userInstitution);

   // UsersNotificationResponse toUserNotificationResponse(List<UserNotificationToSend> userNotificationToSends);

   // UserProduct toOnboardedProduct(OnboardedProduct onboardedProduct);
}
