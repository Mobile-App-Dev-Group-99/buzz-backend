package com.buzzapp.attendance_service.dto;

import lombok.Data;

@Data
public class BiometricVerifyResponse {
    private Long studentId;
    private String studentName;
    private String studentCode;
    private String className;
    private Long templateId;
}
