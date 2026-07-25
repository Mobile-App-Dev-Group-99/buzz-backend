package com.buzzapp.attendance_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StudentUpdateRequest {
    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    private String className;
    private String gender;
    private String studentType;
}
