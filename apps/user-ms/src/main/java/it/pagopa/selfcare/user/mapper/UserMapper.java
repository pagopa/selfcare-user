package it.pagopa.selfcare.user.mapper;

import it.pagopa.selfcare.user.controller.response.OnboardingInfoResponse;
import it.pagopa.selfcare.user.controller.response.UserProductsResponse;
import it.pagopa.selfcare.user.controller.response.UserResponse;
import it.pagopa.selfcare.user.controller.response.UsersNotificationResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "cdi")
public interface UserMapper {
    UserResponse toUserResponse(Object o, String institutionId);

    OnboardingInfoResponse toUserInfoReponse(String userId, Object o);

    UserProductsResponse toUserProductsResponse(String userId, List<Object> objects);

    UsersNotificationResponse toUserNotificationResponse(List<Object> objects);
}
