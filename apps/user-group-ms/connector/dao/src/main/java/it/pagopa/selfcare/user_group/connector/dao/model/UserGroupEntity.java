package it.pagopa.selfcare.user_group.connector.dao.model;


import it.pagopa.selfcare.user_group.connector.api.UserGroupOperations;
import it.pagopa.selfcare.user_group.connector.model.UserGroupStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Set;

@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Document("userGroups")
@FieldNameConstants(onlyExplicitlyIncluded = true)
public class UserGroupEntity implements UserGroupOperations {

    public UserGroupEntity(UserGroupOperations userGroup) {
        this();
        id = userGroup.getId();
        institutionId = userGroup.getInstitutionId();
        productId = userGroup.getProductId();
        name = userGroup.getName();
        description = userGroup.getDescription();
        status = userGroup.getStatus();
        members = userGroup.getMembers();
        createdAt = userGroup.getCreatedAt();
        createdBy = userGroup.getCreatedBy();
        modifiedAt = userGroup.getModifiedAt();
        modifiedBy = userGroup.getModifiedBy();
    }

    @Id
    private String id;
    @FieldNameConstants.Include
    private String institutionId;
    @FieldNameConstants.Include
    private String productId;
    private String name;
    private String description;
    @FieldNameConstants.Include
    private UserGroupStatus status;
    @FieldNameConstants.Include
    private Set<String> members;
    @CreatedDate
    private Instant createdAt;
    @CreatedBy
    private String createdBy;
    @LastModifiedDate
    @FieldNameConstants.Include
    private Instant modifiedAt;
    @LastModifiedBy
    @FieldNameConstants.Include
    private String modifiedBy;


    public static class Fields {
        public static String id = org.springframework.data.mongodb.core.aggregation.Fields.UNDERSCORE_ID;
    }

}
