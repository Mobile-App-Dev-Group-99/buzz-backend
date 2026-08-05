package com.buzzapp.auth_service.controller;

import com.buzzapp.auth_service.dto.*;
import com.buzzapp.auth_service.model.User;
import com.buzzapp.auth_service.repository.UserRepository;
import com.buzzapp.auth_service.services.AuthService;
import com.buzzapp.auth_service.services.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        String message = authService.register(request);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validate(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Missing token");
        }
        try {
            String token = authHeader.substring(7);
            String email = jwtService.extractEmail(token);
            String role = jwtService.extractRole(token);
            Long schoolId = jwtService.extractSchoolId(token);

            Map<String, Object> body = new HashMap<>();
            body.put("email", email);
            body.put("role", role);
            body.put("schoolId", schoolId);

            return ResponseEntity.ok(body);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid token");
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe(Authentication auth) {
        String email = (String) auth.getPrincipal();
        return userRepository.findByEmail(email)
                .map(user -> {
                    Map<String, Object> body = new HashMap<>();
                    body.put("id", user.getId());
                    body.put("email", user.getEmail());
                    body.put("role", user.getRole().toString());
                    body.put("schoolId", user.getSchool_id());
                    body.put("username", user.getUsername());
                    return ResponseEntity.ok((Object) body);
                })
                .orElse(ResponseEntity.status(404).body("User not found"));
    }

    @PostMapping("/onboard-school")
    public ResponseEntity<OnboardSchoolResponse> onboardSchool(@Valid @RequestBody OnboardSchoolRequest request) {
        return ResponseEntity.ok(authService.onboardSchool(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            authService.forgotPassword(request.getEmail());
            return ResponseEntity.ok(Map.of(
                    "message", "A temporary password has been sent to your email. Contact your school admin if you need assistance."
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/admin/reset-password")
    public ResponseEntity<?> adminResetPassword(@Valid @RequestBody ResetPasswordRequest request,
                                                Authentication auth) {
        try {
            authService.adminResetPassword(request.getEmail(), request.getNewPassword(), auth);
            return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                            Authentication auth) {
        String email = (String) auth.getPrincipal();
        try {
            authService.changePassword(email, request.getCurrentPassword(), request.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}