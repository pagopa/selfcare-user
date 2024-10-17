package it.pagopa.selfcare.user.event.mapper;

import it.pagopa.selfcare.user.event.entity.UserGroupEntity;
import it.pagopa.selfcare.user.model.UserGroupNotificationToSend;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi")
public interface UserGroupNotificationMapper {

    @Mapping(target = "id", expression = "java(userGroupEntity.getId().toString())")
    UserGroupNotificationToSend toUserGroupNotificationToSend(UserGroupEntity userGroupEntity);
}
