package com.buzzapp.attendance_service.dto;

import lombok.Data;

@Data
public class StudentTermSummaryResponse {
    private double attendancePercentage;
    private int currentStreak;
    private int totalDaysPresent;
    private int totalSchoolDays;
}