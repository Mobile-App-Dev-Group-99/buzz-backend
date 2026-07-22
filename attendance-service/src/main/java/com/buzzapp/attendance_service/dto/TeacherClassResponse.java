package com.buzzapp.attendance_service.dto;

import lombok.Data;

@Data
public class TeacherClassResponse {
    private Long id;
    private Long teacherUserId;
    private String teacherName;
    private String className;
    private Long schoolId;
}
