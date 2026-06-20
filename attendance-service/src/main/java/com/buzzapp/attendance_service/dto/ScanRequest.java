package com.buzzapp.attendance_service.dto;

import com.buzzapp.attendance_service.model.ScanType;
import lombok.Data;

@Data
public class ScanRequest {
    private Long studentId;
    private ScanType scanType;
    private String gate;
}