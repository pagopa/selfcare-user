package it.pagopa.selfcare.user.controller.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import it.pagopa.selfcare.user.controller.response.institution.OnboardedInstitutionResponse;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OnboardingInfoResponse {
    private String userId;
    private List<OnboardedInstitutionResponse> institutions;
}
