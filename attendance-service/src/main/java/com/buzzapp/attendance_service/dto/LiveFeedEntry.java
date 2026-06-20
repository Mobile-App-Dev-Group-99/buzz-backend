package com.buzzapp.attendance_service.dto;

import com.buzzapp.attendance_service.model.AttendanceStatus;
import com.buzzapp.attendance_service.model.ScanType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LiveFeedEntry {
    private String studentName;
    private String className;
    private String gate;
    private ScanType scanType;
    private AttendanceStatus status;
    private LocalDateTime scannedAt;
}