package it.pagopa.selfcare.user.controller.response.institution;

import lombok.Data;

@Data
public class PaymentServiceProviderResponse {
    private String abiCode;
    private String businessRegisterNumber;
    private String legalRegisterNumber;
    private String legalRegisterName;
    private boolean vatNumberGroup;
}
