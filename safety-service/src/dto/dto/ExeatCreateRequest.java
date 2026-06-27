package com.buzzapp.safety_service.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ExeatCreateRequest {
    private Long studentId;
    private String reason;
    private LocalDateTime expectedReturn;
}