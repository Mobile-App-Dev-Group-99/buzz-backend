package com.buzzapp.attendance_service.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TeacherExeatResponse {
    private Long id;
    private Long studentId;
    private String studentName;
    private String studentClass;
    private String reason;
    private Long approvedBy;
    private String approvedByName;
    private LocalDateTime expectedReturn;
    private LocalDateTime actualReturn;
    private String status;
    private LocalDateTime createdAt;
}
