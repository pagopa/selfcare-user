package it.pagopa.selfcare.user.controller.response.institution;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SupportContact {
    private String supportEmail;
    private String supportPhone;
}
