package com.buzzapp.safety_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SendNotificationRequest {
    @NotNull(message = "Parent ID is required")
    private Long parentId;

    @NotBlank(message = "Message is required")
    private String message;
}
