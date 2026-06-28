package com.buzzapp.safety_service.dto;

import com.buzzapp.safety_service.model.ExeatStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ExeatResponse {
    private Long id;
    private Long studentId;
    private Long schoolId;
    private String reason;
    private Long approvedBy;
    private LocalDateTime expectedReturn;
    private LocalDateTime actualReturn;
    private ExeatStatus status;
    private LocalDateTime createdAt;
}