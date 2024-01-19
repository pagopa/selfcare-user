package it.pagopa.selfcare.user.service;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import it.pagopa.selfcare.user.common.PartyRole;
import it.pagopa.selfcare.user.constant.OnboardedProductState;
import it.pagopa.selfcare.user.controller.response.UserInstitutionResponse;
import it.pagopa.selfcare.user.controller.response.UserProductResponse;

import java.util.List;

public interface UserInstitutionService {

    Uni<UserInstitutionResponse> findById(String id);
    Uni<UserInstitutionResponse> findByInstitutionId(String institutionId);
    Multi<UserInstitutionResponse> findByUserId(String userId);
    Uni<List<String>> getUsersEmailByInstitution(String institutionId);
    Multi<UserProductResponse> getUserProductsByInstitution(String institutionId);
    Multi<UserInstitutionResponse> retrieveUsers(String institutionId, String userId, List<PartyRole> roles, List<OnboardedProductState> states, List<String> products, List<String> productRoles);

}
