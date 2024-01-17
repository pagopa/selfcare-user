package it.pagopa.selfcare.user.controller.response.institution;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Billing {

    private String vatNumber;
    private String recipientCode;
    private boolean publicServices;
}
