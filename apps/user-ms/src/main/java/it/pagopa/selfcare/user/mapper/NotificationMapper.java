package it.pagopa.selfcare.user.mapper;

import it.pagopa.selfcare.user.entity.OnboardedProduct;
import it.pagopa.selfcare.user.model.notification.UserNotificationToSend;
import it.pagopa.selfcare.user.model.notification.UserToNotify;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "cdi", imports = UUID.class)
public interface NotificationMapper {

    @Mapping(source = "onboardedProduct.createdAt", target = "createdAt")
    @Mapping(source = "onboardedProduct.updatedAt", target = "updatedAt")
    @Mapping(source = "onboardedProduct.tokenId", target = "onboardingTokenId")
    @Mapping(source = "onboardedProduct.productId", target = "productId")
    UserNotificationToSend setNotificationDetailsFromOnboardedProduct(UserToNotify user, OnboardedProduct onboardedProduct, String institutionId);
}
