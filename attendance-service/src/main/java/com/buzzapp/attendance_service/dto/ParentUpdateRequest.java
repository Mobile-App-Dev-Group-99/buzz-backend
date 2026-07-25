package com.buzzapp.attendance_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ParentUpdateRequest {
    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    private String phone;

    @Email
    private String email;
}
