package it.pagopa.selfcare.user.entity.filter;


import it.pagopa.selfcare.user.entity.UserInstitution;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static it.pagopa.selfcare.user.entity.filter.UserInstitutionFilter.UserInstitutionFilterEnum.*;

@Builder
public class UserInstitutionFilter {

    private final Object userId;
    private final Object institutionId;
    private final Object userMailUuid;
    private final Object institutionDescription;

    @Getter
    @AllArgsConstructor
    public enum UserInstitutionFilterEnum{
        USER_ID(UserInstitution.Fields.userId.name()),
        USER_MAIL_ID(UserInstitution.Fields.userMailUuid.name()),
        INSTITUTION_ID(UserInstitution.Fields.institutionId.name()),
        INSTITUTION_DESCRIPTION(UserInstitution.Fields.institutionDescription.name());

        private final String description;
    }

    public Map<String, Object> constructMap() {
        Map<String, Object> map = new HashMap<>();

        map.put(USER_ID.getDescription(), userId);
        map.put(USER_MAIL_ID.getDescription(), userMailUuid);
        map.put(INSTITUTION_ID.getDescription(), institutionId);
        map.put(INSTITUTION_DESCRIPTION.getDescription(), institutionDescription);

        map.entrySet().removeIf(e -> Objects.isNull(e.getValue()) ||
                (e.getValue() instanceof Collection && ((Collection) e.getValue()).isEmpty()));

        return map;
    }

}
