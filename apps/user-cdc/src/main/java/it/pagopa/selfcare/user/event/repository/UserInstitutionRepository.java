package it.pagopa.selfcare.user.event.repository;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.model.Updates;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoEntityBase;
import io.smallrye.mutiny.Uni;
import it.pagopa.selfcare.onboarding.common.PartyRole;
import it.pagopa.selfcare.user.event.entity.UserInfo;
import it.pagopa.selfcare.user.event.entity.UserInstitution;
import it.pagopa.selfcare.user.event.entity.UserInstitutionRole;
import it.pagopa.selfcare.user.event.mapper.UserMapper;
import it.pagopa.selfcare.user.model.OnboardedProduct;
import it.pagopa.selfcare.user.model.constants.OnboardedProductState;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.bson.BsonDocument;
import org.bson.BsonDocumentReader;
import org.bson.Document;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.DocumentCodec;
import org.bson.conversions.Bson;

import java.util.*;

import static java.util.function.Predicate.not;


@Slf4j
@RequiredArgsConstructor
@ApplicationScoped
public class UserInstitutionRepository {
    private static final List<OnboardedProductState> VALID_PRODUCT_STATE = List.of(OnboardedProductState.ACTIVE, OnboardedProductState.PENDING, OnboardedProductState.TOBEVALIDATED);

    private final UserMapper userMapper;

    public Uni<Void> updateUser(UserInstitution userInstitution) {
        Optional<OnboardedProductState> optState = retrieveStatusForGivenInstitution(userInstitution.getProducts());
        return UserInfo.findByIdOptional(userInstitution.getUserId())
                .onItem().transformToUni(opt -> opt.map(entityBase -> {
                            if (optState.isPresent() && VALID_PRODUCT_STATE.contains(optState.get())) {
                                Optional<PartyRole> optRole = retrieveRoleForGivenInstitution(userInstitution.getProducts());
                                return optRole.isPresent()
                                    ? updateOrCreateNewUserInfo(opt.get(), userInstitution, optRole.get(), optState.get())
                                    : Uni.createFrom().voidItem();
                            } else {
                                return deleteInstitutionOrAllUserInfo(opt.get(), userInstitution);
                            }
                        })
                        .orElse(createNewUserInfo(userInstitution)));
    }

    private Uni<Void> createNewUserInfo(UserInstitution userInstitution) {
        if(CollectionUtils.isEmpty(userInstitution.getProducts())){
            return Uni.createFrom().voidItem();
        }

        Optional<PartyRole> maxRole = retrieveRoleForGivenInstitution(userInstitution.getProducts());
        if(maxRole.isEmpty()){
            return Uni.createFrom().voidItem();
        }

        UserInstitutionRole institutionRole = userInstitution.getProducts().stream()
                .filter(product -> VALID_PRODUCT_STATE.contains(product.getStatus()))
                .filter(product -> maxRole.get().equals(product.getRole()))
                .map(product -> userMapper.toUserInstitutionRole(userInstitution, product.getRole(), product.getStatus()))
                .findAny().orElse(null);

        if(Objects.isNull(institutionRole)){
            return Uni.createFrom().voidItem();
        }

        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userInstitution.getUserId());
        userInfo.setInstitutions(List.of(institutionRole));

        return UserInfo.persist(userInfo)
                .invoke(() -> log.info(String.format("createNewUserInfo for userId %s and institution %s",
                        userInstitution.getUserId(),userInstitution.getInstitutionId())))
                .replaceWith(Uni.createFrom().voidItem());
    }

    private Uni<Void> deleteInstitutionOrAllUserInfo(ReactivePanacheMongoEntityBase entityBase, UserInstitution userInstitution) {
        return Uni.createFrom().item((UserInfo) entityBase)
                .flatMap(userInfo -> {
                    if (userInfo.getInstitutions().stream()
                            .anyMatch(userInstitutionRole -> userInstitutionRole.getInstitutionId().equalsIgnoreCase(userInstitution.getInstitutionId()))) {

                        userInfo.getInstitutions().removeIf(userInstitutionRole -> userInstitutionRole.getInstitutionId().equalsIgnoreCase(userInstitution.getInstitutionId()));

                        if (CollectionUtils.isEmpty(userInfo.getInstitutions())) {
                            log.info(String.format("deleteInstitutionOrAllUserInfo removing userInfo for userId: %s", userInstitution.getUserId()));
                            return UserInfo.deleteById(userInstitution.getUserId()).replaceWith(Uni.createFrom().voidItem());
                        } else {
                            log.info(String.format("deleteInstitutionOrAllUserInfo removing institution %s for userId %s",
                                    userInstitution.getInstitutionId(), userInstitution.getUserId()));
                            return UserInfo.persistOrUpdate(userInfo);
                        }
                    }
                    return Uni.createFrom().voidItem();
                });
    }

    private Uni<Void> updateOrCreateNewUserInfo(ReactivePanacheMongoEntityBase entityBase, UserInstitution userInstitution, PartyRole role, OnboardedProductState state) {
        return Uni.createFrom().item((UserInfo) entityBase)
                .map(userInfo -> replaceOrAddInstitution(userInfo, userInstitution, role, state))
                .replaceWithVoid();
    }

    private Uni<Void> replaceOrAddInstitution(UserInfo userInfo, UserInstitution userInstitution, PartyRole role, OnboardedProductState state) {
        if (CollectionUtils.isEmpty(userInfo.getInstitutions())) {
            userInfo.setInstitutions(new ArrayList<>());
        }

        userInfo.getInstitutions().stream()
                .filter(userInstitutionRole -> userInstitution.getInstitutionId().equalsIgnoreCase(userInstitutionRole.getInstitutionId()))
                .findFirst()
                .ifPresentOrElse(userInstitutionRole -> {
                            userInstitutionRole.setRole(role.name());
                            userInstitutionRole.setStatus(state.name());
                            userInstitutionRole.setInstitutionName(userInstitution.getInstitutionDescription());
                            userInstitutionRole.setInstitutionRootName(userInstitution.getInstitutionRootName());
                            userInstitutionRole.setUserMailUuid(userInstitution.getUserMailUuid());
                            log.info(String.format("replaceOrAddInstitution execution setting role for userId: %s, institutionId: %s, role: %s",
                                    userInstitution.getUserId(), userInstitution.getInstitutionId(), role.name()));
                            UserInfo.update(userInfo);
                        },
                        () -> {
                            UserInstitutionRole userInstitutionRole = userMapper.toUserInstitutionRole(userInstitution, role, state);
                            log.info(String.format("replaceOrAddInstitution execution adding role for userId: %s, institutionId: %s, role: %s",
                                    userInstitution.getUserId(), userInstitution.getInstitutionId(), role.name()));
                            Bson bson = Updates.addToSet("institutions", userInstitutionRole);
                            BsonDocument bsonDocument = bson.toBsonDocument(BsonDocument.class, MongoClientSettings.getDefaultCodecRegistry());
                            DocumentCodec codec = new DocumentCodec();
                            DecoderContext decoderContext = DecoderContext.builder().build();
                            Document document = codec.decode(new BsonDocumentReader(bsonDocument), decoderContext);
                            UserInfo.update(document).where("_id", userInstitution.getUserId());
                        });

        return Uni.createFrom().voidItem();
    }

    private Optional<PartyRole> retrieveRoleForGivenInstitution(List<OnboardedProduct> products) {
        List<PartyRole> list = products.stream()
                .filter(onboardedProduct -> VALID_PRODUCT_STATE.contains(onboardedProduct.getStatus()))
                .map(OnboardedProduct::getRole)
                .toList();
        return list.isEmpty() ? Optional.empty() : Optional.of(Collections.min(list));

    }

    private Optional<OnboardedProductState> retrieveStatusForGivenInstitution(List<OnboardedProduct> products) {
        return Optional.ofNullable(products)
                .map(productsList -> productsList.stream()
                    .map(OnboardedProduct::getStatus)
                    .toList())
                .filter(not(List::isEmpty))
                .map(Collections::min);
    }
}

