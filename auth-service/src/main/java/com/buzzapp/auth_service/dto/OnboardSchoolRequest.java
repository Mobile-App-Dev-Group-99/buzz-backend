package com.buzzapp.auth_service.dto;

import com.buzzapp.auth_service.model.SchoolLevel;
import lombok.Data;

@Data
public class OnboardSchoolRequest {
    private String schoolName;
    private String location;
    private SchoolLevel level;

    private String adminUsername;
    private String adminEmail;
    private String adminPassword;
}