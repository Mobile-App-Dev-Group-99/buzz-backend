package com.buzzapp.attendance_service.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BiometricRegisterResponse {
    private Long id;
    private Long studentId;
    private LocalDateTime createdAt;
}