package it.pagopa.selfcare.user.mapper;

import it.pagopa.selfcare.user.entity.OnboardedProduct;
import it.pagopa.selfcare.user.model.notification.UserToNotify;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.openapi.quarkus.user_registry_json.model.UserResource;

import java.util.UUID;

@Mapper(componentModel = "cdi", imports = UUID.class)
public interface UserNotificationMapper {

   /* @Mapping(source = "onboardedProduct.role", target = "role")
    @Mapping(source = "onboardedProduct.status", target = "relationshipStatus")
    @Mapping(source = "onboardedProduct.productRole", target = "productRole")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(target = "email", expression = "java(user.getWorkContacts().containsKey(institutionId) ? user.getWorkContacts().get(institutionId).getEmail() : user.getEmail())")
    UserToNotify toUserNotify(UserResource user, OnboardedProduct onboardedProduct, String institutionId);*/

}
