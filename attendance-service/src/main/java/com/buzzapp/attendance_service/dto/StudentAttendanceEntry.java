package com.buzzapp.attendance_service.dto;

import com.buzzapp.attendance_service.model.AttendanceStatus;
import com.buzzapp.attendance_service.model.ScanType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StudentAttendanceEntry {
    private ScanType scanType;
    private AttendanceStatus status;
    private boolean late;
    private LocalDateTime scannedAt;
    private String gate;
}