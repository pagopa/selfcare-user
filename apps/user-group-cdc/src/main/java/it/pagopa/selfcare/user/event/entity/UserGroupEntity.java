package it.pagopa.selfcare.user.event.entity;

import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldNameConstants;

import java.time.Instant;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
@MongoEntity(collection = "userGroups")
@FieldNameConstants(asEnum = true)
public class UserGroupEntity extends ReactivePanacheMongoEntity {

    private String id;
    private String institutionId;
    private String productId;
    private String name;
    private String description;
    private String status;
    private Set<String> members;
    private Instant createdAt;
    private String createdBy;
    private Instant modifiedAt;
    private String modifiedBy;

}