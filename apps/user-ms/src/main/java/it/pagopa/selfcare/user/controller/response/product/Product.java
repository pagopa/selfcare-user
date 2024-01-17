package it.pagopa.selfcare.user.controller.response.product;

import it.pagopa.selfcare.user.common.Env;
import it.pagopa.selfcare.user.common.PartyRole;
import it.pagopa.selfcare.user.constant.OnboardedProductState;
import lombok.Data;

import java.time.OffsetDateTime;

import static it.pagopa.selfcare.user.common.Env.ROOT;

@Data
public class Product {

    private String productId;
    private String tokenId;
    private OnboardedProductState status;
    private String contract;
    private String productRole;
    private PartyRole role;
    private Env env = ROOT;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
