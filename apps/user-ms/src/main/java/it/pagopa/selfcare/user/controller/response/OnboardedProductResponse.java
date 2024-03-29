package it.pagopa.selfcare.user.controller.response;

import it.pagopa.selfcare.onboarding.common.Env;
import it.pagopa.selfcare.onboarding.common.PartyRole;
import it.pagopa.selfcare.user.constant.OnboardedProductState;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OnboardedProductResponse {

    private String productId;
    private String tokenId;
    private OnboardedProductState status;
    private String productRole;
    private PartyRole role;
    private Env env;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
