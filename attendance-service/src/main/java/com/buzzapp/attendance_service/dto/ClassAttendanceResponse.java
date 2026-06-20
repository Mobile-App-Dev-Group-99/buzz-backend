package com.buzzapp.attendance_service.dto;

import lombok.Data;

@Data
public class ClassAttendanceResponse {
    private String className;
    private int present;
    private int total;
    private double percentage;
}