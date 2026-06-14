package com.buzzapp.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OnboardSchoolResponse {
    private String token;
    private String role;
    private String email;
    private Long schoolId;
}