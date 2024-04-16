package it.pagopa.selfcare.user.controller.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryCountResponse {
    private String _id;
    private Integer count;
}
