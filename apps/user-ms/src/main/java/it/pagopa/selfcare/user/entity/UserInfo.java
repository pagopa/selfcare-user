package it.pagopa.selfcare.user.entity;

import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoEntityBase;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.codecs.pojo.annotations.BsonId;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@MongoEntity(collection = "userInfo")
public class UserInfo extends ReactivePanacheMongoEntityBase {

    @BsonId
    private String userId;
    private List<UserInstitutionRole> institutions;

}
