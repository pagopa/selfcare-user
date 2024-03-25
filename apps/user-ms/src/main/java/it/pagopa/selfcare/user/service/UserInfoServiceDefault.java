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
import java.util.Objects;
import java.util.UUID;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

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
    public Multi<UserInfoResponse> findById(List<String> userIds) {
        Multi<UserInfo> userInfos = UserInfo.find("_id in ?1", userIds).stream();
        return userInfos.onItem().transform(userInfoMapper::toResponse);
    }

    @Override
    public Uni<Void> updateUsersEmails(List<String> userIds, int page, int size) {
        Multi<UserInfo> userInfos;
        if(userIds.isEmpty())
            userInfos = UserInfo.findAll().page(page, size).stream();
        else
            userInfos = UserInfo.find("_id in ?1", userIds).stream();
        return userInfos.onItem().transformToUni(userInfo ->
                userRegistryApi.findByIdUsingGET(USERS_FIELD_LIST_WITHOUT_FISCAL_CODE, userInfo.getUserId())
                        .map(this::buildWorkContactsMap)
                        .onItem().transformToUni(userResource ->  userRegistryApi.updateUsingPATCH(userResource.getId().toString(),
                                        MutableUserFieldsDto.builder().workContacts(userResource.getWorkContacts()).build())
                                .replaceWith(userResource))
                                .onFailure().invoke(throwable -> log.error("Impossible to complete PDV patch for user {}. Error: {} ", userInfo.getUserId(), throwable.getMessage()))
                                .onFailure().recoverWithUni(Uni.createFrom().nullItem())
                                .onItem().transformToUni(this::updateUserInstitution)
                                .onFailure()
                                .invoke(throwable -> log.error("Impossible to update UserInstitution for user {}. Error: {} ", userInfo.getUserId(), throwable.getMessage()))
                        .replaceWithVoid())
                .merge().toUni();
    }

    private Uni<Void> updateUserInstitution(UserResource userResource) {

        if(Objects.nonNull(userResource)) {
            final String userId = userResource.getId().toString();
            var filteredMap = userResource.getWorkContacts().entrySet().stream()
                    .filter(entry -> !entry.getKey().contains(EMAIL_UUID_PREFIX))
                    .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

            return Multi.createFrom().items(filteredMap.entrySet().stream()).onItem()
                    .transformToUni(entry -> {
                            final String institutionId = entry.getKey();
                            final String uuid = userResource.getWorkContacts().entrySet().stream().filter(el -> el.getValue().getEmail().getValue().equals(entry.getValue().getEmail().getValue())).map(Map.Entry::getKey).filter(el -> el.contains(EMAIL_UUID_PREFIX)).findFirst().get();
                            if(!uuid.equals(institutionId))
                                return userService.updateUserInstitutionEmail(institutionId, userId, uuid);
                            return Uni.createFrom().voidItem();
                    })
                    .merge().toUni();
        } else {
            log.info("User resource is null");
        }

        return Uni.createFrom().voidItem();

    }

    /*
    private Uni<Void> updateUserInstitution(UserResource userResource) {

        if(Objects.nonNull(userResource)) {
            final String userId = userResource.getId().toString();
            var filteredMap = userResource.getWorkContacts().entrySet().stream()
                    .filter(entry -> !entry.getKey().contains(EMAIL_UUID_PREFIX))
                    .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

            return Multi.createFrom().items(userResource.getWorkContacts().entrySet().stream()).onItem()
                    .transformToUni(entry -> {
                        if (entry.getKey().contains(EMAIL_UUID_PREFIX)) {
                            final String institutionId = filteredMap.entrySet().stream().filter(el -> el.getValue().getEmail().getValue().equals(entry.getValue().getEmail().getValue())).map(Map.Entry::getKey).findFirst().get();
                            return userService.updateUserInstitutionEmail(institutionId, userId, entry.getKey());
                        }
                        return Uni.createFrom().voidItem();
                    })
                    .merge().toUni();
        } else {
            log.info("User resource is null");
        }

        return Uni.createFrom().voidItem();

    }*/


    private UserResource buildWorkContactsMap(UserResource userResource) {
        log.info("Build work contact map");
        if(userResource.getWorkContacts().keySet().stream().anyMatch(s -> s.startsWith(EMAIL_UUID_PREFIX)))
            return userResource;
        Map<String, List<WorkContactResource>> mapGroupedByEmail = userResource.getWorkContacts().values().stream().collect(groupingBy(obj -> obj.getEmail().getValue()));
        mapGroupedByEmail.forEach((key, value) -> userResource.getWorkContacts().put(EMAIL_UUID_PREFIX.concat(UUID.randomUUID().toString()), value.get(0)));
        return userResource;
    }

}
