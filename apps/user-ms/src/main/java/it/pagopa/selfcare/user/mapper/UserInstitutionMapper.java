package it.pagopa.selfcare.user.mapper;

import it.pagopa.selfcare.onboarding.common.PartyRole;
import it.pagopa.selfcare.user.controller.request.AddUserRoleDto;
import it.pagopa.selfcare.user.controller.request.CreateUserDto;
import it.pagopa.selfcare.user.controller.response.UserInstitutionResponse;
import it.pagopa.selfcare.user.entity.UserInstitution;
import it.pagopa.selfcare.user.model.OnboardedProduct;
import it.pagopa.selfcare.user.controller.response.UserInstitutionWithActions;
import it.pagopa.selfcare.user.model.constants.OnboardedProductState;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import software.amazon.awssdk.utils.CollectionUtils;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "jakarta", uses = {OnboardedProductMapper.class})
public interface UserInstitutionMapper {

    @Mapping(target = "id", expression = "java(objectIdToString(userInstitution.getId()))")
    UserInstitutionResponse toResponse(UserInstitution userInstitution);

    @Named("objectIdToString")
    default String objectIdToString(ObjectId objectId) {
        return objectId.toHexString();
    }

    @Mapping(source = "userId", target = "userId")
    @Mapping(source = "createUserDto.institutionId", target = "institutionId")
    @Mapping(source = "createUserDto.institutionDescription", target = "institutionDescription")
    @Mapping(source = "createUserDto.institutionRootName", target = "institutionRootName")
    @Mapping(source = "userMailUuid", target = "userMailUuid")
    @Mapping(target = "products", expression = "java(toNewOnboardedProduct(createUserDto.getProduct()))")
    UserInstitution toNewEntity(CreateUserDto createUserDto, String userId, String userMailUuid);

    @Mapping(source = "userId", target = "userId")
    @Mapping(source = "addUserRoleDto.institutionId", target = "institutionId")
    @Mapping(source = "addUserRoleDto.institutionDescription", target = "institutionDescription")
    @Mapping(source = "addUserRoleDto.institutionRootName", target = "institutionRootName")
    @Mapping(source = "addUserRoleDto.userMailUuid", target = "userMailUuid")
    @Mapping(target = "products", expression = "java(toNewOnboardedProductFromAddUserRole(addUserRoleDto.getProduct()))")
    UserInstitution toNewEntity(AddUserRoleDto addUserRoleDto, String userId);

    UserInstitutionWithActions toUserInstitutionPermission(it.pagopa.selfcare.user.entity.UserInstitution userInstitution);


    default List<OnboardedProduct> toNewOnboardedProduct(CreateUserDto.Product product) {
        if (product == null || CollectionUtils.isNullOrEmpty(product.getProductRoles())) {
            return new ArrayList<>();
        }
        return product.getProductRoles().stream()
                .map(role -> {
                    OnboardedProduct onboardedProduct = buildOnboardedProduct();
                    onboardedProduct.setProductId(product.getProductId());
                    onboardedProduct.setTokenId(product.getTokenId());
                    onboardedProduct.setProductRole(role);
                    onboardedProduct.setToAddOnAggregates(product.getToAddOnAggregates());
                    if(StringUtils.isNotBlank(product.getRole())) {
                        onboardedProduct.setRole(PartyRole.valueOf(product.getRole()));
                    }
                    return onboardedProduct;
                })
                .collect(Collectors.toList());
    }

    default List<OnboardedProduct> toNewOnboardedProductFromAddUserRole(AddUserRoleDto.Product product) {
        if (product == null || CollectionUtils.isNullOrEmpty(product.getProductRoles())){
            return new ArrayList<>();
        }

        return product.getProductRoles().stream()
                .map(role -> {
                    OnboardedProduct onboardedProduct = buildOnboardedProduct();
                    onboardedProduct.setProductId(product.getProductId());
                    onboardedProduct.setTokenId(product.getTokenId());
                    onboardedProduct.setProductRole(role);
                    onboardedProduct.setToAddOnAggregates(product.getToAddOnAggregates());
                    if(StringUtils.isNotBlank(product.getRole())) {
                        onboardedProduct.setRole(PartyRole.valueOf(product.getRole()));
                    }
                    return onboardedProduct;
                })
                .collect(Collectors.toList());
    }

    default OnboardedProduct buildOnboardedProduct(){
        OffsetDateTime now = java.time.OffsetDateTime.now();
        OnboardedProduct onboardedProduct = new OnboardedProduct();
        onboardedProduct.setStatus(OnboardedProductState.ACTIVE);
        onboardedProduct.setEnv(it.pagopa.selfcare.onboarding.common.Env.ROOT);
        onboardedProduct.setCreatedAt(now);
        onboardedProduct.setUpdatedAt(now);
        return onboardedProduct;
    }
}
