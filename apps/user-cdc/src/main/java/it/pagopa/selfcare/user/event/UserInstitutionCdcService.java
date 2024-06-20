package it.pagopa.selfcare.user.event;

import com.azure.data.tables.TableClient;
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.TableServiceException;
import com.microsoft.applicationinsights.TelemetryClient;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.mongodb.client.model.changestream.FullDocument;
import io.quarkus.mongodb.ChangeStreamOptions;
import io.quarkus.mongodb.reactive.ReactiveMongoClient;
import io.quarkus.mongodb.reactive.ReactiveMongoCollection;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.Startup;
import io.quarkus.runtime.configuration.ConfigUtils;
import io.smallrye.mutiny.Multi;
import it.pagopa.selfcare.user.UserUtils;
import it.pagopa.selfcare.user.client.EventHubRestClient;
import it.pagopa.selfcare.user.event.entity.UserInstitution;
import it.pagopa.selfcare.user.event.mapper.NotificationMapper;
import it.pagopa.selfcare.user.event.repository.UserInstitutionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.openapi.quarkus.user_registry_json.api.UserApi;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeoutException;

import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;
import static it.pagopa.selfcare.user.event.constant.CdcStartAtConstant.*;
import static it.pagopa.selfcare.user.model.constants.EventsMetric.*;
import static it.pagopa.selfcare.user.model.constants.EventsName.EVENT_USER_CDC_NAME;
import static java.util.Arrays.asList;

@Startup
@Slf4j
@ApplicationScoped
public class UserInstitutionCdcService {

    private static final String COLLECTION_NAME = "userInstitutions";
    private static final String OPERATION_NAME = "USER-CDC-UserInfoUpdate";
    private static final String USERINSTITUTION_FAILURE_MECTRICS = "UserInfoUpdate_failures";
    private static final String USERINSTITUTION_SUCCESS_MECTRICS = "UserInfoUpdate_successes";
    private static final String SEND_EVENTS_FAILURE_MECTRICS = "SendEvents_failures";
    private static final String SEND_EVENTS_SUCCESS_MECTRICS = "SendEvents_successes";
    public static final String USERS_FIELD_LIST_WITHOUT_FISCAL_CODE = "name,familyName,email,workContacts";
    public static final String METRIC_TRUE = "TRUE";
    public static final String METRIC_FALSE = "FALSE";

    private final TelemetryClient telemetryClient;

    private final TableClient tableClient;
    private final String mongodbDatabase;
    private final ReactiveMongoClient mongoClient;
    private final UserInstitutionRepository userInstitutionRepository;

    private final Integer retryMinBackOff;
    private final Integer retryMaxBackOff;
    private final Integer maxRetry;


    @RestClient
    @Inject
    UserApi userRegistryApi;
    @RestClient
    @Inject
    EventHubRestClient eventHubRestClient;

    private final NotificationMapper notificationMapper;


    public UserInstitutionCdcService(ReactiveMongoClient mongoClient,
                                     @ConfigProperty(name = "quarkus.mongodb.database") String mongodbDatabase,
                                     @ConfigProperty(name = "user-cdc.retry.min-backoff") Integer retryMinBackOff,
                                     @ConfigProperty(name = "user-cdc.retry.max-backoff") Integer retryMaxBackOff,
                                     @ConfigProperty(name = "user-cdc.retry") Integer maxRetry,
                                     @ConfigProperty(name = "user-cdc.send-events.watch.enabled") Boolean sendEventsEnabled,
                                     UserInstitutionRepository userInstitutionRepository,
                                     TelemetryClient telemetryClient,
                                     TableClient tableClient, NotificationMapper notificationMapper) {
        this.mongoClient = mongoClient;
        this.mongodbDatabase = mongodbDatabase;
        this.userInstitutionRepository = userInstitutionRepository;
        this.maxRetry = maxRetry;
        this.retryMaxBackOff = retryMaxBackOff;
        this.retryMinBackOff = retryMinBackOff;
        this.telemetryClient = telemetryClient;
        this.tableClient = tableClient;
        this.notificationMapper = notificationMapper;
        telemetryClient.getContext().getOperation().setName(OPERATION_NAME);
        initOrderStream(sendEventsEnabled);
    }

    private void initOrderStream(Boolean sendEventsEnabled) {
        log.info("Starting initOrderStream ... ");

        //Retrieve last resumeToken for watching collection at specific operation
        String resumeToken = null;

        if(!ConfigUtils.getProfiles().contains("test")) {
            try {
                TableEntity cdcStartAtEntity = tableClient.getEntity(CDC_START_AT_PARTITION_KEY, CDC_START_AT_ROW_KEY);
                if (Objects.nonNull(cdcStartAtEntity))
                    resumeToken = (String) cdcStartAtEntity.getProperty(CDC_START_AT_PROPERTY);
            } catch (TableServiceException e) {
                log.warn("Table StarAt not found, it is starting from now ...");
            }
        }

        // Initialize watching collection
        ReactiveMongoCollection<UserInstitution> dataCollection = getCollection();
        ChangeStreamOptions options = new ChangeStreamOptions()
                .fullDocument(FullDocument.UPDATE_LOOKUP);
        if(Objects.nonNull(resumeToken))
            options = options.resumeAfter(BsonDocument.parse(resumeToken));

        Bson match = Aggregates.match(Filters.in("operationType", asList("update", "replace", "insert")));
        Bson project = Aggregates.project(fields(include("_id", "ns", "documentKey", "fullDocument")));
        List<Bson> pipeline = Arrays.asList(match, project);

        Multi<ChangeStreamDocument<UserInstitution>> publisher = dataCollection.watch(pipeline, UserInstitution.class, options);
        publisher.subscribe().with(
                this::consumerUserInstitutionRepositoryEvent,
                failure -> {
                    log.error("Error during subscribe collection, exception: {} , message: {}", failure.toString(), failure.getMessage());
                    constructMapAndTrackEvent(failure.getClass().toString(), "FALSE", USERINSTITUTION_FAILURE_MECTRICS);
                    Quarkus.asyncExit();
                });

        if(sendEventsEnabled) {
            publisher.subscribe().with(
                    this::consumerToSendScUserEvent,
                    failure -> {
                        log.error("Error during subscribe collection, exception: {} , message: {}", failure.toString(), failure.getMessage());
                        constructMapAndTrackEvent(failure.getClass().toString(), "FALSE", EVENTS_USER_INSTITUTION_FAILURE);
                        Quarkus.asyncExit();
                    });
        }

        log.info("Completed initOrderStream ... ");
    }

    private ReactiveMongoCollection<UserInstitution> getCollection() {
        return mongoClient
                .getDatabase(mongodbDatabase)
                .getCollection(COLLECTION_NAME, UserInstitution.class);
    }

    protected void consumerUserInstitutionRepositoryEvent(ChangeStreamDocument<UserInstitution> document) {

        assert document.getFullDocument() != null;
        assert document.getDocumentKey() != null;

        log.info("Starting consumerUserInstitutionRepositoryEvent ... ");

        userInstitutionRepository.updateUser(document.getFullDocument())
                .onFailure().retry().withBackOff(Duration.ofSeconds(retryMinBackOff), Duration.ofHours(retryMaxBackOff)).atMost(maxRetry)
                .subscribe().with(
                        result -> {
                            log.info("UserInfo collection successfully updated from UserInstitution document having id: {}", document.getDocumentKey().toJson());
                            updateLastResumeToken(document.getResumeToken());
                            constructMapAndTrackEvent(document.getDocumentKey().toJson(), METRIC_TRUE, USERINSTITUTION_SUCCESS_MECTRICS);
                        },
                        failure -> {
                            log.error("Error during UserInfo collection updating, from UserInstitution document having id: {} , message: {}", document.getDocumentKey().toJson(), failure.getMessage());
                            constructMapAndTrackEvent(document.getDocumentKey().toJson(), METRIC_FALSE, USERINSTITUTION_FAILURE_MECTRICS);
                        });
    }

    private void updateLastResumeToken(BsonDocument resumeToken) {
        // Table CdCStartAt will be updated with the last resume token
        Map<String, Object> properties = new HashMap<>();
        properties.put(CDC_START_AT_PROPERTY, resumeToken.toJson());

        TableEntity tableEntity = new TableEntity(CDC_START_AT_PARTITION_KEY, CDC_START_AT_ROW_KEY)
                .setProperties(properties);
        tableClient.upsertEntity(tableEntity);

    }

    private void constructMapAndTrackEvent(String documentKey, String success, String... metrics) {
        Map<String, String> propertiesMap = new HashMap<>();
        propertiesMap.put("documentKey", documentKey);
        propertiesMap.put("success", success);

        Map<String, Double> metricsMap = new HashMap<>();
        Arrays.stream(metrics).forEach(metricName -> metricsMap.put(metricName, 1D));
        telemetryClient.trackEvent(EVENT_USER_CDC_NAME, propertiesMap, metricsMap);
    }



    public void consumerToSendScUserEvent(ChangeStreamDocument<UserInstitution> document) {

        assert document.getFullDocument() != null;
        assert document.getDocumentKey() != null;
        UserInstitution userInstitutionChanged = document.getFullDocument();

        log.info("Starting consumerUserInstitutionRepositoryEvent ... ");

        userRegistryApi.findByIdUsingGET(USERS_FIELD_LIST_WITHOUT_FISCAL_CODE, userInstitutionChanged.getUserId())
                .onFailure(this::checkIfIsRetryableException)
                    .retry().withBackOff(Duration.ofSeconds(retryMinBackOff), Duration.ofSeconds(retryMaxBackOff)).atMost(maxRetry)
                .onItem().transformToUni(userResource -> Multi.createFrom().iterable(UserUtils.groupingProductAndReturnMinStateProduct(userInstitutionChanged.getProducts()))
                        .map(onboardedProduct -> notificationMapper.toUserNotificationToSend(userInstitutionChanged, onboardedProduct, userResource))
                        .onItem().transformToUniAndMerge(userNotificationToSend -> eventHubRestClient.sendMessage(userNotificationToSend)
                            .onItem().invoke(() -> constructMapAndTrackEvent(document.getDocumentKey().toJson(), METRIC_TRUE, EVENTS_USER_INSTITUTION_PRODUCT_SUCCESS))
                            .onFailure().invoke(() -> constructMapAndTrackEvent(document.getDocumentKey().toJson(), METRIC_TRUE, EVENTS_USER_INSTITUTION_PRODUCT_FAILURE)))
                        .toUni()
                )
                .subscribe().with(
                        result -> {
                            log.info("SendEvents successfully performed from UserInstitution document having id: {}", document.getDocumentKey().toJson());
                            constructMapAndTrackEvent(document.getDocumentKey().toJson(), METRIC_TRUE, EVENTS_USER_INSTITUTION_SUCCESS);
                        },
                        failure -> {
                            log.error("Error during SendEvents from UserInstitution document having id: {} , message: {}", document.getDocumentKey().toJson(), failure.getMessage());
                            constructMapAndTrackEvent(document.getDocumentKey().toJson(), METRIC_FALSE, SEND_EVENTS_FAILURE_MECTRICS);
                        });
    }

    private boolean checkIfIsRetryableException(Throwable throwable) {
        return throwable instanceof TimeoutException ||
                (throwable instanceof WebApplicationException webApplicationException && webApplicationException.getResponse().getStatus() == 429);
    }
}
