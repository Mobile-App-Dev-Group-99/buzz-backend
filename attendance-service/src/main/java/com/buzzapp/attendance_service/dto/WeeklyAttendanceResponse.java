package com.buzzapp.attendance_service.dto;

import lombok.Data;

import java.util.Map;

@Data
public class WeeklyAttendanceResponse {
    private Map<String, Double> rateByDay; // e.g. {"MONDAY": 94.5, "TUESDAY": 89.0}
}