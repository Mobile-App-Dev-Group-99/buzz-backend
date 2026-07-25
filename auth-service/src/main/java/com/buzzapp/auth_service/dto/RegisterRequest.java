package com.buzzapp.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
   @NotBlank(message = "Username is required")
   private String username;

   @NotBlank(message = "Email is required")
   @Email(message = "Invalid email format")
   private String email;

   @NotBlank(message = "Password is required")
   @Size(min = 6, message = "Password must be at least 6 characters")
   private String password;

   @NotBlank(message = "Role is required")
   private String role;

   private Long schoolId;

   // Student profile fields
   private String firstName;
   private String lastName;
   private String className;
   private String gender;
   private String studentType;

   // Parent profile fields
   private String phone;
}
