package com.buzzapp.auth_service.dto;

public class LoginResponse {
    private String token;
    private String email;
    private String username;

    public LoginResponse(String email, String token, String username) {
        this.email = email;
        this.token = token;
        this.username = username;
    }


}
