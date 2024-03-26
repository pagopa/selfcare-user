package it.pagopa.selfcare.user.mapper;

import it.pagopa.selfcare.onboarding.common.PartyRole;
import it.pagopa.selfcare.user.constant.OnboardedProductState;
import it.pagopa.selfcare.user.controller.request.CreateUserDto;
import it.pagopa.selfcare.user.controller.response.*;
import it.pagopa.selfcare.user.entity.OnboardedProduct;
import it.pagopa.selfcare.user.entity.UserInfo;
import it.pagopa.selfcare.user.entity.UserInstitution;
import it.pagopa.selfcare.user.model.notification.UserNotificationToSend;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import org.openapi.quarkus.user_registry_json.model.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Mapper(componentModel = "cdi")
public interface UserMapper {

    UserNotificationResponse toUserNotification(UserNotificationToSend user);

    @Mapping(source = "userResource.fiscalCode", target = "taxCode")
    @Mapping(source = "userResource.familyName", target = "surname", qualifiedByName = "fromCertifiableString")
    @Mapping(source = "userResource.name", target = "name", qualifiedByName = "fromCertifiableString")
    @Mapping(target = "email", expression = "java(retrieveMailFromWorkContacts(userResource.getWorkContacts(), userMailUuid))")
    @Mapping(target = "workContacts", expression = "java(toWorkContacts(userResource.getWorkContacts()))")
    UserResponse toUserResponse(UserResource userResource, String userMailUuid);
    @Mapping(target = "email", expression = "java(retrieveCertifiedMailFromWorkContacts(userResource, userMailUuid))")
    UserDetailResponse toUserDetailResponse(UserResource userResource, String userMailUuid);

    default Map<String, String> toWorkContacts(Map<String, WorkContactResource> workContactResourceMap) {
        if (workContactResourceMap == null){
            return Collections.emptyMap();
        }
        return workContactResourceMap.entrySet().stream()
                .filter(entry -> entry.getValue().getEmail() != null && entry.getValue().getEmail().getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getEmail().getValue()));
    }


    @Named("fromCertifiableString")
    default String fromCertifiableString(CertifiableFieldResourceOfstring certifiableFieldResourceOfstring) {
        return Optional.ofNullable(certifiableFieldResourceOfstring).map(CertifiableFieldResourceOfstring::getValue).orElse(null);
    }

    UserDetailResponse toUserResponse(UserResource userResource);

    @Named("retrieveMailFromWorkContacts")
    default String retrieveMailFromWorkContacts(Map<String, WorkContactResource> map, String userMailUuid){
        if(map!=null && !map.isEmpty() && map.containsKey(userMailUuid)){
            return map.get(userMailUuid).getEmail().getValue();
        }
        return null;
    }


    @Named("retrieveCertifiedMailFromWorkContacts")
    default CertifiableFieldResponse<String> retrieveCertifiedMailFromWorkContacts(UserResource userResource, String userMailUuid){
        if(userResource.getWorkContacts()!=null && !userResource.getWorkContacts().isEmpty() && userResource.getWorkContacts().containsKey(userMailUuid)){
            return new CertifiableFieldResponse<>(userResource.getWorkContacts().get(userMailUuid).getEmail().getValue(), userResource.getWorkContacts().get(userMailUuid).getEmail().getCertification());
        } else if (null!=userResource.getEmail()) {
            return new CertifiableFieldResponse<>(userResource.getEmail().getValue(), userResource.getEmail().getCertification());
        }
        return null;
    }

    MutableUserFieldsDto toMutableUserFieldsDto(UserResource userResource);

    @Mapping(source = "user.birthDate", target = "birthDate", qualifiedByName = "toCertifiableLocalDate")
    @Mapping(source = "user.familyName", target = "familyName",  qualifiedByName = "toCertifiableString")
    @Mapping(source = "user.name", target = "name",  qualifiedByName = "toCertifiableString")
    @Mapping(source = "user.fiscalCode", target = "fiscalCode")
    @Mapping(source = "workContactResource", target = "workContacts")
    SaveUserDto toSaveUserDto(CreateUserDto.User user, Map<String, WorkContactResource> workContactResource);

    @Named("toCertifiableLocalDate")
    default CertifiableFieldResourceOfLocalDate toLocalTime(String time) {
        var certifiableFieldResourceOfLocalDate = new CertifiableFieldResourceOfLocalDate();
        certifiableFieldResourceOfLocalDate.setValue(LocalDate.parse(time));
        certifiableFieldResourceOfLocalDate.setCertification(CertifiableFieldResourceOfLocalDate.CertificationEnum.NONE);
        return certifiableFieldResourceOfLocalDate;
    }

    @Named("toCertifiableString")
    default CertifiableFieldResourceOfstring toCertString(String value) {
        var certifiableFieldResourceOfstring = new CertifiableFieldResourceOfstring();
        certifiableFieldResourceOfstring.setValue(value);
        certifiableFieldResourceOfstring.setCertification(CertifiableFieldResourceOfstring.CertificationEnum.NONE);
        return certifiableFieldResourceOfstring;
    }

    OnboardedProductMapper productMapper = Mappers.getMapper(OnboardedProductMapper.class);
    @Mapping(target = "id", expression = "java(userInstitution.getId().toString())")
    @Mapping(target = "role", expression = "java(getMaxRole(userInstitution.getProducts()))")
    @Mapping(target = "status", expression = "java(getMaxStatus(userInstitution.getProducts()))")
    @Mapping(target = "products", expression = "java(productMapper.toList(userInstitution.getProducts()))")
    @Mapping(target = "userResponse", expression = "java(toUserResponse(userResource, userInstitution.getUserMailUuid()))")
    UserDataResponse toUserDataResponse(UserInstitution userInstitution, UserResource userResource);

    @Named("getMaxStatus")
    default String getMaxStatus(List<OnboardedProduct> onboardedProductList){
        List<OnboardedProductState> onboardedProductStateList = onboardedProductList.stream().map(OnboardedProduct::getStatus).toList();
        return Collections.min(onboardedProductStateList).name();
    }

    @Named("getMaxRole")
    default String getMaxRole(List<OnboardedProduct> onboardedProductList){
        List<PartyRole> partyRoleList = onboardedProductList.stream().map(OnboardedProduct::getRole).toList();
        return Collections.min(partyRoleList).name();
    }

    UserInfoResponse toUserInfoResponse(UserInfo userInfo);
}
