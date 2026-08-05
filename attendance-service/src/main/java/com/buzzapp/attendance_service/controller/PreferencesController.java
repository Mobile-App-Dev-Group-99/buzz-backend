package com.buzzapp.attendance_service.controller;

import com.buzzapp.attendance_service.dto.NotificationPreferenceItem;
import com.buzzapp.attendance_service.dto.NotificationPreferencesRequest;
import com.buzzapp.attendance_service.service.PreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notification/preferences")
@RequiredArgsConstructor
public class PreferencesController {

    private final PreferenceService preferenceService;

    @GetMapping
    public ResponseEntity<List<NotificationPreferenceItem>> getPreferences(@RequestParam String role,
                                                                           @RequestParam Long recipientId,
                                                                           Authentication auth) {
        return ResponseEntity.ok(preferenceService.getPreferences(role, recipientId));
    }

    @PutMapping
    public ResponseEntity<List<NotificationPreferenceItem>> updatePreferences(
            @RequestBody NotificationPreferencesRequest request) {
        return ResponseEntity.ok(preferenceService.updatePreferences(request));
    }
}
