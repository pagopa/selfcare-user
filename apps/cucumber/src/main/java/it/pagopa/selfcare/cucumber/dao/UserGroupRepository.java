package it.pagopa.selfcare.cucumber.dao;

import it.pagopa.selfcare.cucumber.model.UserGroupEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserGroupRepository extends MongoRepository<UserGroupEntity, String> {

}
