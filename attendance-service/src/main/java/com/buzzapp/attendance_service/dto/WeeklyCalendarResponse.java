package com.buzzapp.attendance_service.dto;

import com.buzzapp.attendance_service.model.AttendanceStatus;
import lombok.Data;

import java.time.LocalDate;
import java.util.Map;

@Data
public class WeeklyCalendarResponse {
    private Map<LocalDate, AttendanceStatus> week; // each day → status
}