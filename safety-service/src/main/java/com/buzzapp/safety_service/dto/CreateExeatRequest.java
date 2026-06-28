package com.buzzapp.safety_service.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CreateExeatRequest {
    private Long studentId;
    private String reason;
    private LocalDateTime expectedReturn;
}