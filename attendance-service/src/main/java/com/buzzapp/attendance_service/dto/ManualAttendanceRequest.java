package com.buzzapp.attendance_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ManualAttendanceRequest {
    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotBlank(message = "Status is required")
    private String status;
}
