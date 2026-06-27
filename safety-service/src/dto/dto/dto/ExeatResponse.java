package com.buzzapp.safety_service.dto;

import com.buzzapp.safety_service.model.ExeatStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ExeatResponse {
    private Long id;
    private Long studentId;
    private Long approvedBy;
    private String reason;
    private LocalDateTime expectedReturn;
    private LocalDateTime actualReturn;
    private ExeatStatus status;
    private LocalDateTime createdAt;
}