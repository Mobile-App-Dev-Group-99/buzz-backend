package com.buzzapp.safety_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegisterDeviceTokenRequest {

    @NotBlank(message = "Role is required")
    private String role;

    @NotNull(message = "Recipient ID is required")
    private Long recipientId;

    @NotBlank(message = "Token is required")
    private String token;

    private String platform;
}
