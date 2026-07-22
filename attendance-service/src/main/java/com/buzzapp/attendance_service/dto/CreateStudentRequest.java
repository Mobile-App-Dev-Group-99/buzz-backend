package com.buzzapp.attendance_service.dto;

import lombok.Data;

@Data
public class CreateStudentRequest {
    private String firstName;
    private String lastName;
    private String className;
    private String gender;
    private String studentType;
}
