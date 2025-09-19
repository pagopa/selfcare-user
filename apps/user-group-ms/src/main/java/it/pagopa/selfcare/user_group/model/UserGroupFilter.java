package it.pagopa.selfcare.user_group.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class UserGroupFilter {
    private String institutionId;
    private String parentInstitutionId;
    private String productId;
    private String userId;
    private List<UserGroupStatus> status;

    public UserGroupFilter(){
        this.status = Collections.emptyList();
    }

}
