package com.buzzapp.auth_service.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String password;
    private String email;

}
