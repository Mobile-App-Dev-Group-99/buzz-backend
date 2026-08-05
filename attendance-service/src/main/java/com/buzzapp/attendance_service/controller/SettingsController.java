package com.buzzapp.attendance_service.controller;

import com.buzzapp.attendance_service.dto.SchoolSettingsRequest;
import com.buzzapp.attendance_service.dto.SchoolSettingsResponse;
import com.buzzapp.attendance_service.service.SettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/school")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;

    @GetMapping("/settings")
    public ResponseEntity<SchoolSettingsResponse> getSettings(Authentication auth) {
        Long schoolId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(settingsService.getSettings(schoolId));
    }

    @PutMapping("/settings")
    public ResponseEntity<SchoolSettingsResponse> updateSettings(@RequestBody SchoolSettingsRequest request,
                                                                 Authentication auth) {
        Long schoolId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(settingsService.updateSettings(schoolId, request));
    }
}
