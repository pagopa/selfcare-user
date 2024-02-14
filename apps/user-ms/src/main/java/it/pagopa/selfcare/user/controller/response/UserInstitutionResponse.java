package it.pagopa.selfcare.user.controller.response;

import lombok.Data;

import java.util.List;

@Data
public class UserInstitutionResponse {

    private String id;
    private String userId;
    private String institutionId;
    private String institutionDescription;
    private String institutionRootName;
    private String idMail;
    private List<OnboardedProductResponse> products;

}
