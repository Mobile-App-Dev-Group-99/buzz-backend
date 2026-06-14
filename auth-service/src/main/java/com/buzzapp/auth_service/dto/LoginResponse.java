package com.buzzapp.auth_service.dto;

public class LoginResponse {
    private String token;
    private String email;
    private String role;

    public LoginResponse(String email, String token, String role) {
        this.email = email;
        this.token = token;
        this.role = role;
    }


}
