package com.buzzapp.attendance_service.dto;

import lombok.Data;

@Data
public class ManualAttendanceRequest {
    private Long studentId;
    private String status; // PRESENT, LATE, ABSENT
}
