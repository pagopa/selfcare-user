package it.pagopa.selfcare.user.service;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import it.pagopa.selfcare.user.entity.UserInstitution;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.openapi.quarkus.user_registry_json.api.UserApi;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RequiredArgsConstructor
@ApplicationScoped
public class UserServiceImpl implements UserService {

    @RestClient
    @Inject
    UserApi userRegistryApi;

    private final UserInstitutionService userInstitutionService;

    @Override
    public Uni<List<String>> getUsersEmails(String institutionId, String productId) {
        Map<String, Object> parametersMap = Map.of("institutionId", institutionId, "products.productId", productId);
        Multi<UserInstitution> userInstitutions =  userInstitutionService.findAllWithFilter(parametersMap);
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
}