package com.buzzapp.auth_service.dto;

import lombok.Data;

@Data
public class RegisterRequest {
   private String username;
   private String email;
   private String password;
   private String role;
   private Long schoolId;

   // Student profile fields (optional, used when role=STUDENT)
   private String firstName;
   private String lastName;
   private String className;
   private String gender;
   private String studentType;

   // Parent profile fields (optional, used when role=PARENT)
   private String phone;
}