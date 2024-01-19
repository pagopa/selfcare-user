package it.pagopa.selfcare.user.service;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import it.pagopa.selfcare.user.common.PartyRole;
import it.pagopa.selfcare.user.constant.OnboardedProductState;
import it.pagopa.selfcare.user.controller.response.UserInstitutionResponse;
import it.pagopa.selfcare.user.controller.response.UserProductResponse;
import it.pagopa.selfcare.user.entity.UserInstitution;
import it.pagopa.selfcare.user.mapper.OnboardedProductMapper;
import it.pagopa.selfcare.user.mapper.UserInstitutionMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.openapi.quarkus.user_registry_json.api.UserApi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static it.pagopa.selfcare.user.utils.QueryUtils.buildQuery;
import static it.pagopa.selfcare.user.utils.QueryUtils.cleanParameterMap;

@Slf4j
@ApplicationScoped
public class UserInstitutionServiceDefault implements UserInstitutionService {

    @Inject
    private UserInstitutionMapper userInstitutionMapper;

    @Inject
    private OnboardedProductMapper onboardedProductMapper;

    @RestClient
    @Inject
    private UserApi userRegistryApi;

    private static final String USERS_WORKS_FIELD_LIST = "fiscalCode,familyName,name,workContacts";

    @Override
    public Uni<UserInstitutionResponse> findById(String id) {
        Uni<UserInstitution> userInstitution = UserInstitution.findById(new ObjectId(id));
        return userInstitution.onItem().transform(userInstitutionMapper::toResponse);
    }

    @Override
    public Uni<UserInstitutionResponse> findByInstitutionId(String institutionId) {
        Uni<UserInstitution> userInstitution = UserInstitution.find("institutionId", institutionId).firstResult();
        return userInstitution.onItem().transform(userInstitutionMapper::toResponse);
    }

    @Override
    public Multi<UserInstitutionResponse> findByUserId(String userId) {
        Multi<UserInstitution> userInstitutions = UserInstitution.find("userId", userId).stream();
        return userInstitutions.onItem().transform(userInstitutionMapper::toResponse);
    }

    @Override
    public Uni<List<String>> getUsersEmailByInstitution(String institutionId) {
        Multi<UserInstitution> userInstitutions =  UserInstitution.find("institutionId", institutionId).stream();
        return userInstitutions.onItem()
                .transformToUni(obj -> userRegistryApi.findByIdUsingGET("workContacts", obj.getUserId()))
                .merge()
                .filter(userResource -> Objects.nonNull(userResource.getWorkContacts())
                        && userResource.getWorkContacts().containsKey(institutionId))
                .map(user -> user.getWorkContacts().get(institutionId))
                .filter(workContract -> StringUtils.isNotBlank(workContract.getEmail().getValue()))
                .map(workContract -> workContract.getEmail().getValue())
                .collect().asList();

    }

    @Override
    public Multi<UserProductResponse> getUserProductsByInstitution(String institutionId) {
        Multi<UserInstitution> userInstitutions =  UserInstitution.find("institutionId", institutionId).stream();
        return userInstitutions.onItem()
                .transformToUni(userInstitution -> userRegistryApi.findByIdUsingGET(USERS_WORKS_FIELD_LIST, userInstitution.getUserId())
                        .map(userResource -> UserProductResponse.builder()
                                .id(userResource.getId().toString())
                                .name(userResource.getName().getValue())
                                .email(userResource.getEmail().getValue())
                                .surname(userResource.getFamilyName().getValue())
                                .taxCode(userResource.getFiscalCode())
                                .products(onboardedProductMapper.toList(userInstitution.getProducts()))
                                .build()))
                .merge();
    }

    @Override
    public Multi<UserInstitutionResponse> retrieveUsers(String institutionId,
                                                        String userId,
                                                        List<PartyRole> roles,
                                                        List<OnboardedProductState> states,
                                                        List<String> products,
                                                        List<String> productRoles) {

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("institutionId", institutionId);
        parameters.put("userId", userId);
        parameters.put("products.status", states);
        parameters.put("products.role", roles);
        parameters.put("products.productId", products);
        parameters.put("products.productRole", productRoles);

        Map<String, Object> nonNullParams = cleanParameterMap(parameters);
        String query = buildQuery(nonNullParams);

        Multi<UserInstitution> userInstitutions = UserInstitution.find(query, nonNullParams).stream();
        return userInstitutions.onItem().transform(userInstitutionMapper::toResponse);
    }
}
