package com.buzzapp.auth_service.controller;

import com.buzzapp.auth_service.dto.UpdateSchoolRequest;
import com.buzzapp.auth_service.model.School;
import com.buzzapp.auth_service.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/school")
@RequiredArgsConstructor
public class SchoolController {

    private final AuthService authService;

    @GetMapping("/me")
    public ResponseEntity<?> getMySchool(Authentication auth) {
        Long schoolId = (Long) auth.getPrincipal();
        try {
            School school = authService.getSchool(schoolId);
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("id", school.getId());
            body.put("name", school.getName());
            body.put("location", school.getLocation());
            body.put("level", school.getLevel() != null ? school.getLevel().name() : null);
            return ResponseEntity.ok(body);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateMySchool(@Valid @RequestBody UpdateSchoolRequest request,
                                            Authentication auth) {
        Long schoolId = (Long) auth.getPrincipal();
        try {
            authService.updateSchool(schoolId, request);
            School school = authService.getSchool(schoolId);
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("id", school.getId());
            body.put("name", school.getName());
            body.put("location", school.getLocation());
            body.put("level", school.getLevel() != null ? school.getLevel().name() : null);
            return ResponseEntity.ok(body);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
