package com.buzzapp.attendance_service.dto;

import lombok.Data;

@Data
public class CreateParentRequest {
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private String password;
}
