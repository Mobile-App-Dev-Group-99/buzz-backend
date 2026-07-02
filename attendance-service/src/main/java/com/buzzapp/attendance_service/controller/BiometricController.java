package com.buzzapp.attendance_service.controller;

import com.buzzapp.attendance_service.dto.BiometricRegisterRequest;
import com.buzzapp.attendance_service.dto.BiometricRegisterResponse;
import com.buzzapp.attendance_service.service.BiometricService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/biometric")
@RequiredArgsConstructor
public class BiometricController {

    private final BiometricService biometricService;

    @PostMapping("/register")
    public ResponseEntity<BiometricRegisterResponse> register(@RequestBody BiometricRegisterRequest request,
                                                              Authentication auth) {
        Long schoolId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(biometricService.registerTemplate(request, schoolId));
    }
}