package it.pagopa.selfcare.user.service;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import it.pagopa.selfcare.onboarding.common.PartyRole;
import it.pagopa.selfcare.user.constant.PermissionTypeEnum;
import it.pagopa.selfcare.user.controller.request.UpdateDescriptionDto;
import it.pagopa.selfcare.user.controller.response.UserInstitutionResponse;
import it.pagopa.selfcare.user.entity.UserInstitution;
import it.pagopa.selfcare.user.model.constants.OnboardedProductState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface UserInstitutionService {

    Uni<UserInstitutionResponse> findById(String id);

    Uni<UserInstitutionResponse> findByInstitutionId(String institutionId);

    Multi<UserInstitutionResponse> findByUserId(String userId);

    Multi<UserInstitution> paginatedFindAllWithFilter(Map<String, Object> queryParameter, Integer page, Integer size);

    Multi<UserInstitution> findAllWithFilter(Map<String, Object> queryParameter);

    Multi<UserInstitution> findUserInstitutionsAfterDateWithFilter(Map<String,Object> queryParameter, LocalDateTime fromDate);

    Uni<UserInstitution> retrieveFirstFilteredUserInstitution(Map<String, Object> queryParameter);

    Uni<Long> deleteUserInstitutionProduct(String userId, String institutionId, String productId);

    Uni<Long> updateUserStatusWithOptionalFilterByInstitutionAndProduct(String userId, String institutionId, String productId, PartyRole role, String productRole, OnboardedProductState status);

    Uni<Long> updateUserCreatedAtByInstitutionAndProduct(String institutionId, List<String> userIds, String productId, LocalDateTime createdAt);

    Multi<UserInstitution> findUserInstitutionsAfterDateWithFilter(Map<String, Object> queryParameter, LocalDateTime fromDate, Integer page);

    Uni<Integer> pageCountUserInstitutionsAfterDateWithFilter(Map<String, Object> queryParameter, LocalDateTime fromDate);

    Uni<List<UserInstitution>> retrieveFilteredUserInstitution(Map<String, Object> queryParameter);

    Uni<UserInstitution> findByUserIdAndInstitutionId(String userId, String institutionId);

    Uni<UserInstitution> persistOrUpdate(UserInstitution userInstitution);

    Uni<Boolean> existsValidUserProduct(String userId, String institutionId, String productId, PermissionTypeEnum permission);

    Uni<Long> updateInstitutionDescription(String institutionId, UpdateDescriptionDto updateDescriptionDto);

}
