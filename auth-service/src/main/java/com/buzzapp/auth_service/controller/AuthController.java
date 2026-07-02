package com.buzzapp.auth_service.controller;

import com.buzzapp.auth_service.dto.*;
import com.buzzapp.auth_service.services.AuthService;
import com.buzzapp.auth_service.services.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request){
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request){
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

   @PostMapping("/onboard-school")
   public ResponseEntity<OnboardSchoolResponse> onboardSchool(@RequestBody OnboardSchoolRequest request) {
        return ResponseEntity.ok(authService.onboardSchool(request));
}

}
