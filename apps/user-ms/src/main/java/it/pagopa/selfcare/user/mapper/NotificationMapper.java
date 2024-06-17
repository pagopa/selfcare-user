package it.pagopa.selfcare.user.mapper;

import it.pagopa.selfcare.user.entity.OnboardedProduct;
import it.pagopa.selfcare.user.entity.UserInstitution;
import it.pagopa.selfcare.user.model.UserNotificationToSend;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.openapi.quarkus.user_registry_json.model.UserResource;

import java.util.UUID;

@Mapper(componentModel = "cdi", imports = UUID.class)
public interface NotificationMapper {

    @Mapping(target = "onboardingTokenId", source = "product.tokenId")
    @Mapping(target = "productId", source = "product.productId")
    @Mapping(target = "createdAt", source = "product.createdAt")
    @Mapping(target = "updatedAt", expression = "java((null == product.getUpdatedAt()) ? product.getCreatedAt() : product.getUpdatedAt())")
    @Mapping(target = "user.role", source = "product.role")
    @Mapping(target = "user.productRole", source = "product.productRole")
    @Mapping(target = "user.relationshipStatus", source = "product.status")
    @Mapping(target = "user.userId", source = "userResource.id")
    @Mapping(target = "user.name", source = "userResource.name.value")
    @Mapping(target = "user.familyName", source = "userResource.familyName.value")
    @Mapping(target = "user.email", source = "userResource.email.value")
    @Mapping(target = "id", expression = "java(null)")
    UserNotificationToSend toUserNotificationToSend(UserInstitution userInstitution, OnboardedProduct product, UserResource userResource);


}
