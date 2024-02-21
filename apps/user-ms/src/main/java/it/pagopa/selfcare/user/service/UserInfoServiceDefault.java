package it.pagopa.selfcare.user.service;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import it.pagopa.selfcare.user.controller.response.UserInfoResponse;
import it.pagopa.selfcare.user.entity.UserInfo;
import it.pagopa.selfcare.user.mapper.UserInfoMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.openapi.quarkus.user_registry_json.api.UserApi;

import java.util.stream.Collectors;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class UserInfoServiceDefault implements UserInfoService {

    private final UserInfoMapper userInfoMapper;
    private static final String USERS_FIELD_LIST_WITHOUT_FISCAL_CODE = "name,familyName,email,workContacts";

    @RestClient
    @Inject
    private UserApi userRegistryApi;

    @Override
    public Uni<UserInfoResponse> findById(String userId) {
        Uni<UserInfo> userInfo = UserInfo.findById(userId);
        return userInfo.onItem().transform(userInfoMapper::toResponse);
    }

    @Override
    public Uni<Void> updateUserEmail(int page, int size) {
        Multi<UserInfo> userInfos = UserInfo.findAll().page(page, size).stream();
        userInfos.onItem().transformToUni(userInfo -> userInstitution -> userRegistryApi
                .findByIdUsingGET(USERS_FIELD_LIST_WITHOUT_FISCAL_CODE, userInfo.getUserId())
                .map(userResource -> userResource.getWorkContacts()
                        .values().stream().collect(Collectors.groupingBy(obj -> obj.getEmail().getValue()))
                 )
                .onItem().invoke(map -> userRegistryApi.updateUsingPATCH(userInfo.getUserId(), ))
                .onItem().invoke(this::updateInstitution));
        return Uni.createFrom().voidItem();
    }

    private void updateInstitution() {

    }

}
