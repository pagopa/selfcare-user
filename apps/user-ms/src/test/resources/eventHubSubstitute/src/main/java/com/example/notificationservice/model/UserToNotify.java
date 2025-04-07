package com.example.notificationservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserToNotify {
    private String userId;
    private String name;
    private String familyName;
    private String email;
    private String mobilePhone;
    private String role;
    private String productRole;
    private List<String> roles;
    private OnboardedProductState relationshipStatus;
}