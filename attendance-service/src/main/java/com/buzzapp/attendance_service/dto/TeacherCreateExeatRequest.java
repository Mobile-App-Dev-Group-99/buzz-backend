package com.buzzapp.attendance_service.dto;

import lombok.Data;

@Data
public class TeacherCreateExeatRequest {
    private Long studentId;
    private String reason;
    private String expectedReturn; // ISO datetime string
}
