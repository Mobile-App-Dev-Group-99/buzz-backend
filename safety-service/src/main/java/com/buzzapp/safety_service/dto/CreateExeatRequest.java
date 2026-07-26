package com.buzzapp.safety_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CreateExeatRequest {
    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotBlank(message = "Reason is required")
    private String reason;

    private String notes;

    private LocalDateTime expectedReturn;
}
