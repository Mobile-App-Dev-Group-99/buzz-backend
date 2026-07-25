package com.buzzapp.safety_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExeatStatusUpdateRequest {
    @NotNull
    private String status;
}
