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
import org.openapi.quarkus.user_registry_json.model.MutableUserFieldsDto;
import org.openapi.quarkus.user_registry_json.model.UserResource;
import org.openapi.quarkus.user_registry_json.model.WorkContactResource;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static java.util.stream.Collectors.groupingBy;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class UserInfoServiceDefault implements UserInfoService {

    private final UserInfoMapper userInfoMapper;
    private final UserService userService;
    private static final String USERS_FIELD_LIST_WITHOUT_FISCAL_CODE = "name,familyName,email,workContacts";
    private static final String EMAIL_UUID_PREFIX = "ID_MAIL#";

    @RestClient
    @Inject
    private UserApi userRegistryApi;

    @Override
    public Uni<UserInfoResponse> findById(String userId) {
        Uni<UserInfo> userInfo = UserInfo.findById(userId);
        return userInfo.onItem().transform(userInfoMapper::toResponse);
    }

    @Override
    public Uni<Void> updateUsersEmails(int page, int size) {
        Multi<UserInfo> userInfos = UserInfo.findAll().page(page, size).stream();
        return userInfos.onItem().invoke(userInfo ->
                userRegistryApi.findByIdUsingGET(USERS_FIELD_LIST_WITHOUT_FISCAL_CODE, userInfo.getUserId())
                .map(this::buildWorkContactsMap)
                .invoke(userResource -> userRegistryApi.updateUsingPATCH(userInfo.getUserId(), MutableUserFieldsDto.builder().workContacts(userResource.getWorkContacts()).build())
                        .onFailure().invoke(throwable -> log.error("Impossible to complete PDV patch for user {}. Error: {} ", userInfo.getUserId(), throwable.getMessage(), throwable)))
                        .invoke(getUserResourceConsumer(userInfo)).replaceWith(Uni.createFrom().voidItem())
                        .onFailure().invoke(throwable -> log.error("Impossible to update UserInstitution with userId {} and institutionId {}", userInfo.getUserId(), throwable.getMessage(), throwable)))
                .onItem().ignoreAsUni();
    }

    private UserResource buildWorkContactsMap(UserResource userResource) {
        Map<String, List<WorkContactResource>> newMap = userResource.getWorkContacts().values().stream().collect(groupingBy(obj -> obj.getEmail().getValue()));
        newMap.forEach((key, value) -> userResource.getWorkContacts().put(EMAIL_UUID_PREFIX.concat(UUID.randomUUID().toString()), value.get(0)));
        return userResource;
    }

    private Consumer<UserResource> getUserResourceConsumer(UserInfo userInfo) {
        return userResource -> {
            var copiedMap = userResource.getWorkContacts();
            userResource.getWorkContacts().forEach((key, value) -> {
                if (key.contains(EMAIL_UUID_PREFIX)) {
                    String institutionId = copiedMap.entrySet().stream().filter(el -> el.getValue().getEmail().getValue().equals(value.getEmail().getValue())).map(Map.Entry::getKey).findFirst().get();
                    userService.updateUserInstitutionEmail(institutionId, userInfo.getUserId(), key);
                }
            });
        };
    }

}
