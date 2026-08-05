package com.buzzapp.auth_service.dto;

import com.buzzapp.auth_service.model.SchoolLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateSchoolRequest {
    @NotBlank(message = "School name is required")
    private String name;

    @NotBlank(message = "Location is required")
    private String location;

    @NotNull(message = "Level is required")
    private SchoolLevel level;
}
