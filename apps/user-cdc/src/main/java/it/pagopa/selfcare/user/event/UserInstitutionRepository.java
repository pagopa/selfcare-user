package it.pagopa.selfcare.user.event;

import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.mongodb.client.model.changestream.FullDocument;
import io.quarkus.mongodb.ChangeStreamOptions;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoEntityBase;
import io.quarkus.mongodb.reactive.ReactiveMongoClient;
import io.quarkus.mongodb.reactive.ReactiveMongoCollection;
import io.quarkus.runtime.Startup;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import it.pagopa.selfcare.onboarding.common.PartyRole;
import it.pagopa.selfcare.user.event.constant.OnboardedProductState;
import it.pagopa.selfcare.user.event.entity.OnboardedProduct;
import it.pagopa.selfcare.user.event.entity.UserInfo;
import it.pagopa.selfcare.user.event.entity.UserInstitution;
import it.pagopa.selfcare.user.event.entity.UserInstitutionRole;
import it.pagopa.selfcare.user.event.mapper.UserMapper;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.*;

import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;
import static java.util.Arrays.asList;

@Startup
@Slf4j
@ApplicationScoped
public class UserInstitutionRepository {
    private static final String COLLECTION_NAME = "userInstitutions";
    private static final List<OnboardedProductState> VALID_PRODUCT_STATE = List.of(OnboardedProductState.ACTIVE, OnboardedProductState.PENDING, OnboardedProductState.TOBEVALIDATED);

    private final String mongodbDatabase;
    private final ReactiveMongoClient mongoClient;
    private final UserMapper userMapper;

    public UserInstitutionRepository(ReactiveMongoClient mongoClient,
                                     @ConfigProperty(name = "quarkus.mongodb.database") String mongodbDatabase,
                                     UserMapper userMapper) {
        this.mongoClient = mongoClient;
        this.mongodbDatabase = mongodbDatabase;
        this.userMapper = userMapper;
        initOrderStream();
    }

    private void initOrderStream() {
        ReactiveMongoCollection<UserInstitution> dataCollection = getCollection();
        ChangeStreamOptions options = new ChangeStreamOptions().fullDocument(FullDocument.UPDATE_LOOKUP);

        Bson match = Aggregates.match(Filters.in("operationType", asList("update", "replace", "insert")));
        Bson project = Aggregates.project(fields(include("_id", "ns", "documentKey", "fullDocument")));
        List<Bson> pipeline = Arrays.asList(match, project);

        Multi<ChangeStreamDocument<UserInstitution>> publisher = dataCollection.watch(pipeline, UserInstitution.class, options);
        publisher.subscribe().with(this::consumerUserInstitutionRepositoryEvent);
    }

    private ReactiveMongoCollection<UserInstitution> getCollection() {
        return mongoClient
                .getDatabase(mongodbDatabase)
                .getCollection(COLLECTION_NAME, UserInstitution.class);
    }

    protected void consumerUserInstitutionRepositoryEvent(ChangeStreamDocument<UserInstitution> document) {
        assert document.getFullDocument() != null;
        updateUser(document.getFullDocument());
    }

    public void updateUser(UserInstitution userInstitution) {
        OnboardedProductState state = retrieveStatusForGivenInstitution(userInstitution.getProducts());
        UserInfo.findByIdOptional(userInstitution.getUserId())
                .onItem().transformToUni(opt -> {
                    if (VALID_PRODUCT_STATE.contains(state)) {
                        PartyRole role = retrieveRoleForGivenInstitution(userInstitution.getProducts());
                        return updateOrCreateNewUserInfo(opt, userInstitution, role, state);
                    } else {
                        return deleteInstitutionOrAllUserInfo(opt, userInstitution);
                    }
                })
                .subscribe().with(
                        result -> log.info("UserInfo collection successfully updated"),
                        failure -> log.error("Error during UserInfo collection updating, message:" + failure.getMessage()));

    }

    private Uni<Void> deleteInstitutionOrAllUserInfo(Optional<ReactivePanacheMongoEntityBase> opt, UserInstitution userInstitution) {
        return opt.map(UserInfo.class::cast)
                .map(userInfo -> {
                    if(userInfo.getInstitutions().stream()
                            .anyMatch(userInstitutionRole -> userInstitutionRole.getInstitutionId().equalsIgnoreCase(userInstitution.getInstitutionId()))){

                        List<UserInstitutionRole> roleList = new ArrayList<>(userInfo.getInstitutions());
                        roleList.removeIf(userInstitutionRole -> userInstitutionRole.getInstitutionId().equalsIgnoreCase(userInstitution.getInstitutionId()));
                        userInfo.setInstitutions(roleList);

                        if(roleList.isEmpty()){
                            return UserInfo.deleteById(userInstitution.getUserId()).replaceWith(Uni.createFrom().voidItem());
                        }else{
                            return UserInfo.persistOrUpdate(userInfo);
                        }
                    }
                    return Uni.createFrom().voidItem();
                })
                .orElse(Uni.createFrom().voidItem());
    }

    private Uni<Void> updateOrCreateNewUserInfo(Optional<ReactivePanacheMongoEntityBase> opt, UserInstitution userInstitution, PartyRole role, OnboardedProductState state) {
        return opt.map(UserInfo.class::cast)
                .map(userInfo -> replaceOrAddInstitution(userInfo, userInstitution, role, state))
                .orElse(userMapper.toNewUserInfo(userInstitution, role, state))
                .persistOrUpdate()
                .replaceWith(Uni.createFrom().voidItem());
    }

    private UserInfo replaceOrAddInstitution(UserInfo userInfo, UserInstitution userInstitution, PartyRole role, OnboardedProductState state) {
        userInfo.getInstitutions().stream()
                .filter(userInstitutionRole -> userInstitution.getInstitutionId().equalsIgnoreCase(userInstitutionRole.getInstitutionId()))
                .findFirst()
                .ifPresentOrElse(userInstitutionRole -> {
                            userInstitutionRole.setRole(role);
                            userInstitutionRole.setState(state);
                        },
                        () -> {
                            List<UserInstitutionRole> roleList = new ArrayList<>(userInfo.getInstitutions());
                            roleList.add(userMapper.toUserInstitutionRole(userInstitution, role, state));
                            userInfo.setInstitutions(roleList);
                        });
        return userInfo;
    }

    private PartyRole retrieveRoleForGivenInstitution(List<OnboardedProduct> products) {
        List<PartyRole> list = products.stream()
                .filter(onboardedProduct -> VALID_PRODUCT_STATE.contains(onboardedProduct.getStatus()))
                .map(OnboardedProduct::getRole)
                .toList();
        return Collections.min(list);

    }

    private OnboardedProductState retrieveStatusForGivenInstitution(List<OnboardedProduct> products) {
        List<OnboardedProductState> list = products.stream()
                .map(OnboardedProduct::getStatus)
                .toList();
        return Collections.min(list);

    }
}
