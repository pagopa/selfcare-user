package it.pagopa.selfcare.user.service;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import it.pagopa.selfcare.user.common.PartyRole;
import it.pagopa.selfcare.user.constant.OnboardedProductState;
import it.pagopa.selfcare.user.entity.UserInstitution;
import it.pagopa.selfcare.user.exception.InvalidRequestException;
import it.pagopa.selfcare.user.mapper.UserMapper;
import it.pagopa.selfcare.user.model.OnboardingInfo;
import it.pagopa.selfcare.user.model.UserInstitutionBinding;
import it.pagopa.selfcare.user.model.notification.UserNotificationToSend;
import it.pagopa.selfcare.user.model.institution.Institution;
import it.pagopa.selfcare.user.util.QueryUtils;
import it.pagopa.selfcare.user.util.UserUtils;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.openapi.quarkus.user_registry_json.api.UserApi;
import org.openapi.quarkus.user_registry_json.model.UserResource;

import java.util.*;

import static it.pagopa.selfcare.user.constant.CustomError.*;


@RequiredArgsConstructor
@ApplicationScoped
public class UserServiceImpl implements UserService {

    private final UserApi userRegistryApi;
    private final DaoService daoService;
    private final UserMapper userMapper;
    private final UserUtils userUtils;
    private final QueryUtils queryUtils;
    private static final List<String> VALID_USER_RELATIONSHIPS = List.of(OnboardedProductState.ACTIVE.name(), OnboardedProductState.DELETED.name(), OnboardedProductState.SUSPENDED.name());
    public static final String USERS_FIELD_LIST = "fiscalCode,name,familyName,email,workContacts";
    public static final String USERS_FIELD_LIST_WITHOUT_FISCAL_CODE = "name,familyName,email,workContacts";

    /**
     * The updateUserStatus function updates the status of a user's onboarded product.
     */
    @Override
    public Uni<Void> updateUserStatus(String userId, String institutionId, String productId, PartyRole role, String productRole, OnboardedProductState status) {
        return userUtils.checkRoles(productId, role, productRole)
                .map(aBoolean -> {
                    if (Boolean.FALSE.equals(aBoolean)) {
                        throw new InvalidRequestException(PRODUCT_ROLE_NOT_FOUND.getMessage(), PRODUCT_ROLE_NOT_FOUND.getCode());
                    }
                    return aBoolean;
                })
                .onItem().transformToUni(aBoolean -> daoService.updateUserStatusDao(userId, institutionId, productId, role, productRole, status))
                .replaceWith(Uni.createFrom().voidItem());
    }

    /**
     * The findAll function is used to find all the users that are eligible for a notification.
     */
    @Override
    public Uni<List<UserNotificationToSend>> findPaginatedUserNotificationToSend(Integer size, Integer page, String productId) {
        Map<String, Object> queryParameter = queryUtils.createMapForUserQueryParameter(null, null, productId, VALID_USER_RELATIONSHIPS, null, null);
        return daoService.paginatedFindAllWithFilter(queryParameter, size, page)
                .onItem().transformToMulti(Multi.createFrom()::iterable)
                .onItem().transformToUni(userInstitution -> userRegistryApi.findByIdUsingGET(USERS_FIELD_LIST_WITHOUT_FISCAL_CODE, userInstitution.getUserId())
                        .map(userResource -> userUtils.constructUserNotificationToSend(userInstitution, userResource, productId)))
                .merge()
                .toUni();
    }

    @Override
    public Uni<UserResource> retrievePerson(String userId, String productId, String institutionId) {
        Map<String, Object> queryParameter = queryUtils.createMapForUserQueryParameter(userId, institutionId, productId, null, null, null);
        return daoService.retrieveFirstFilteredUserInstitution(queryParameter)
                .onItem().transformToUni(userInstitution -> userRegistryApi.findByIdUsingGET(USERS_FIELD_LIST, userInstitution.getUserId()));
    }

    @Override
    public Uni<UserInstitution> retrieveBindings(String institutionId, String userId, String[] states) {

        List<String> relationshipStates = Arrays.asList(states);
        Map<String, Object> queryParameter = queryUtils.createMapForUserQueryParameter(userId, institutionId, null, relationshipStates, null, null);
        return daoService.retrieveFirstFilteredUserInstitution(queryParameter)
                .map(userInstitution -> userUtils.filterProduct(userInstitution, states));

    }

    //MANCA L'EVENTUALE CHIAMATA ALLA GET INSTITUTION VALUTARE SE TALE API VA PORTATA QUI O NO.
    @Override
    public Uni<List<OnboardingInfo>> getUserInfo(String userId, String institutionId, String[] states) {
        List<String> relationshipStates = Arrays.asList(states);
        Map<String, Object> queryParameter = queryUtils.createMapForUserQueryParameter(userId, institutionId, null, relationshipStates, null, null);
        Document query = queryUtils.buildQueryDocument(queryParameter);

        List<OnboardingInfo> response = new ArrayList<>();
        return daoService.runUserInstitutionFindQuery(query, null).list()
                .onItem().transformToMulti( Multi.createFrom()::iterable )
                .map(userInstitution -> {
                    Institution institution = new Institution();
                    userInstitution.getProducts().forEach(onboardedProduct -> {
                        OnboardingInfo onboardingInfo = new OnboardingInfo(userId, institution, new UserInstitutionBinding(institutionId, userMapper.toOnboardedProduct(onboardedProduct)));
                        response.add(onboardingInfo);
                    });
                    return response;
                })
                .toUni();
    }

}
