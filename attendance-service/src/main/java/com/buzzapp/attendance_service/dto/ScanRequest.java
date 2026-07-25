package com.buzzapp.attendance_service.dto;

import com.buzzapp.attendance_service.model.ScanType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ScanRequest {
    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Scan type is required")
    private ScanType scanType;

    private String gate;
}
