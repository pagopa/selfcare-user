package it.pagopa.selfcare.user.service;

import com.mongodb.client.model.Sorts;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoEntityBase;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheQuery;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import it.pagopa.selfcare.user.controller.response.UserInfoResponse;
import it.pagopa.selfcare.user.entity.UserInfo;
import it.pagopa.selfcare.user.entity.UserInstitution;
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
import software.amazon.awssdk.utils.CollectionUtils;
import software.amazon.awssdk.utils.StringUtils;

import java.util.*;

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
        Multi<UserInfo> userInfos = UserInfo.find("_id in [?1]", userIds).stream();
        return userInfos.onItem().transform(userInfoMapper::toResponse);
    }

    @Override
    public Uni<Void> updateUsersFromInstitutions(int page, int size, String userId) {
        List<UserInstitution> userInstitutions = new ArrayList<>();
        return retrieveUserInstitution(page, size, userInstitutions, userId)
                .onItem().transform(userInstitutions1 -> userInstitutions.stream().collect(groupingBy(UserInstitution::getUserId)))
                .onItem().transformToMulti(userInstitutionsMap -> Multi.createFrom().iterable(userInstitutionsMap.entrySet().stream().toList()))
                .onItem().transformToUniAndMerge(entry -> userRegistryApi.findByIdUsingGET(USERS_FIELD_LIST_WITHOUT_FISCAL_CODE, entry.getKey())
                        .map(this::buildWorkContactsMap)
                        .onItem().transformToUni(userResource -> userRegistryApi.updateUsingPATCH(userResource.getId().toString(),
                                        MutableUserFieldsDto.builder().workContacts(userResource.getWorkContacts()).build())
                                .replaceWith(userResource))
                        .onFailure().invoke(throwable -> log.error("Impossible to complete PDV patch for user {}. Error: {} ", entry.getKey(), throwable.getMessage()))
                        .onItem().transformToUni(userResource -> updateUserInstitutions(userResource, entry.getValue()))
                        .onFailure().invoke(throwable -> log.error("Impossible to update UserInstitution for user {}. Error: {} ", entry.getKey(), throwable.getMessage()))
                        .onFailure().recoverWithNull())
                .toUni().replaceWithVoid();
    }

    private Uni<Void> updateUserInstitutions(UserResource userResource, List<UserInstitution> value) {
        if(CollectionUtils.isNullOrEmpty(userResource.getWorkContacts())) {
            return Uni.createFrom().voidItem();
        }

        return Multi.createFrom().iterable(value)
                .onItem().transformToUni(userInstitution -> {
                    var filteredMap = userResource.getWorkContacts().entrySet().stream()
                            .filter(entry -> entry.getKey().contains(EMAIL_UUID_PREFIX))
                            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

                    var institutionMail = userResource.getWorkContacts().entrySet().stream()
                            .filter(entry -> entry.getKey().equalsIgnoreCase(userInstitution.getInstitutionId()))
                            .findFirst()
                            .map(stringWorkContactResourceEntry -> stringWorkContactResourceEntry.getValue().getEmail().getValue())
                            .orElse("");

                    if (!CollectionUtils.isNullOrEmpty(filteredMap)) {
                        return filteredMap.entrySet().stream()
                                .filter(entry -> entry.getValue().getEmail().getValue().equalsIgnoreCase(institutionMail))
                                .findFirst()
                                .map(stringWorkContactResourceEntry -> {
                                    userInstitution.setUserMailUuid(stringWorkContactResourceEntry.getKey());
                                    return UserInstitution.persistOrUpdate(userInstitution);
                                })
                                .orElseThrow(() -> new RuntimeException(String.format("User mail for institutionId %s and UserId %s not found in workContacts", userInstitution.getInstitutionId(), userInstitution.getUserId())))
                                .onFailure().recoverWithUni(() -> {
                                    log.error("User mail for institutionId {} not found in workContacts", userInstitution.getInstitutionId());
                                    return Uni.createFrom().nullItem();
                                });
                    }
                    return Uni.createFrom().nullItem();
                })
                .merge().toUni();
    }

    private Uni<List<UserInstitution>> retrieveUserInstitution(int page, int size, List<UserInstitution> userInstitutions, String userId) {
        if (page == 4) {
            return Uni.createFrom().item(userInstitutions);
        }

        Uni<List<UserInstitution>> uniUserInstitutions;
        ReactivePanacheQuery<ReactivePanacheMongoEntityBase> entityBase;

        if (StringUtils.isNotBlank(userId)) {
            entityBase = UserInstitution.find(UserInstitution.Fields.userId.name(), userId)
                    .page(page, size);
            uniUserInstitutions = entityBase.list();
        } else {
            entityBase = UserInstitution.find("{ userMailUuid: { $exists: false } }", Sorts.ascending("_id"))
                    .page(page, size);
            uniUserInstitutions = entityBase.list();
        }

        return uniUserInstitutions
                .onItem().transformToMulti(userInstitutions1 -> Multi.createFrom().iterable(userInstitutions1))
                .onItem().transform(userInstitution -> {
                    userInstitutions.add(userInstitution);
                    return userInstitution;
                })
                .collect().asList().replaceWith(entityBase)
                .onItem().transformToUni(ReactivePanacheQuery::hasNextPage)
                .onItem().transformToUni(aBoolean -> {
                    if (aBoolean) {
                        return retrieveUserInstitution(page + 1, size, userInstitutions, userId);
                    }
                    return Uni.createFrom().item(userInstitutions);
                });
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

            //Filtro i workContacts senza prefisso
            var filteredMap = userResource.getWorkContacts().entrySet().stream()
                    .filter(entry -> !entry.getKey().contains(EMAIL_UUID_PREFIX))
                    .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

            return Multi.createFrom().items(filteredMap.entrySet().stream()).onItem()
                    .transformToUni(entry -> {
                            final String institutionId = entry.getKey();
                            try {
                                final String uuid = userResource.getWorkContacts().entrySet().stream()
                                        .filter(el -> el.getValue().getEmail().getValue().equals(entry.getValue().getEmail().getValue()))
                                        .map(Map.Entry::getKey).filter(el -> el.contains(EMAIL_UUID_PREFIX))
                                        .findFirst()
                                        .get();
                                if(!uuid.equals(institutionId))
                                    return userService.updateUserInstitutionEmail(institutionId, userId, uuid);
                            } catch (Exception e) {
                                log.error("Entry present in PDV but not yet mapped with UUID");
                            }
                            return Uni.createFrom().voidItem();
                    })
                    .merge().toUni();
        } else {
            log.info("User resource is null");
        }

        return Uni.createFrom().voidItem();

    }

    private UserResource buildWorkContactsMap(UserResource userResource) {
        log.info("Build work contact map");

        Map<String, List<WorkContactResource>> mapGroupedByEmail;
        if (!CollectionUtils.isNullOrEmpty(userResource.getWorkContacts())) {
            if (userResource.getWorkContacts().keySet().stream().noneMatch(s -> s.startsWith(EMAIL_UUID_PREFIX))) {
                mapGroupedByEmail = userResource.getWorkContacts().values().stream()
                        .filter(workContactResource -> Objects.nonNull(workContactResource.getEmail()))
                        .collect(groupingBy(obj -> obj.getEmail().getValue()));
                mapGroupedByEmail.forEach((key, value) -> userResource.getWorkContacts().put(EMAIL_UUID_PREFIX.concat(UUID.randomUUID().toString()), value.get(0)));
            }
        }
        return userResource;
    }

}
