package it.pagopa.selfcare.user_group.connector.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class UserGroupFilter {
    private String institutionId;
    private String productId;
    private String userId;
    private List<UserGroupStatus> status;

    public UserGroupFilter(String institutionId, String productId, UUID userId, List<UserGroupStatus> status) {
        this.institutionId = institutionId;
        this.productId = productId;
        this.userId = userId != null ? userId.toString() : null;
        this.status = CollectionUtils.isEmpty(status) ? Collections.emptyList() : status;
    }

    public UserGroupFilter(){
        this.status = Collections.emptyList();
    }

}
