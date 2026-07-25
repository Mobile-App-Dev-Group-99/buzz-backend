package com.buzzapp.safety_service.controller;

import com.buzzapp.safety_service.dto.*;
import com.buzzapp.safety_service.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    public ResponseEntity<NotificationResponse> send(
            @Valid @RequestBody SendNotificationRequest request,
            Authentication authentication) {
        Long schoolId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(notificationService.sendNotification(request, schoolId));
    }

    @GetMapping("/parent/{parentId}")
    public ResponseEntity<List<NotificationResponse>> getByParent(
            @PathVariable Long parentId,
            Authentication authentication) {
        Long schoolId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(notificationService.getNotificationsByParent(parentId, schoolId));
    }
}