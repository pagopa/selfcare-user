package it.pagopa.selfcare.user.model;

import it.pagopa.selfcare.user.model.institution.Institution;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OnboardingInfo {
    private String userId;
    private Institution institution;
    private UserInstitutionBinding binding;
}
