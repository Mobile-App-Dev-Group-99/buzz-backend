package com.buzzapp.auth_service.dto;

import lombok.Data;

@Data
public class RegisterRequest {
   private String username;
   private String email;
   private String password;
   private String role;
   private Long schoolId;
}