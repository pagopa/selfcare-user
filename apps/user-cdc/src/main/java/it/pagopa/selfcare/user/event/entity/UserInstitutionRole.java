package it.pagopa.selfcare.user.event.entity;

import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.codecs.pojo.annotations.BsonProperty;

@Data
@FieldNameConstants(asEnum = true)
@BsonDiscriminator
public class UserInstitutionRole {

    @BsonProperty("institutionId")
    private String institutionId;
    @BsonProperty("institutionName")
    private String institutionName;
    @BsonProperty("institutionRootName")
    private String institutionRootName;
    @BsonProperty("userMailUuid")
    private String userMailUuid;
    @BsonProperty("role")
    private String role;
    @BsonProperty("status")
    private String status;

}
