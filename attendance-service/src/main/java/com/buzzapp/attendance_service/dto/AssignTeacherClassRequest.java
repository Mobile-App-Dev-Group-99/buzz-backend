package com.buzzapp.attendance_service.dto;

import lombok.Data;

@Data
public class AssignTeacherClassRequest {
    private Long teacherUserId;
    private String className;
}
