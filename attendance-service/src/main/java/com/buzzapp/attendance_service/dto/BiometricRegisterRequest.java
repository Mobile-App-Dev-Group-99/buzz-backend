package com.buzzapp.attendance_service.dto;

import lombok.Data;

@Data
public class BiometricRegisterRequest {
    private Long studentId;
    private String template;
}