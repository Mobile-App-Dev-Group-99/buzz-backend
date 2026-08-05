package com.buzzapp.safety_service.controller;

import com.buzzapp.safety_service.dto.*;
import com.buzzapp.safety_service.model.DeviceToken;
import com.buzzapp.safety_service.repository.DeviceTokenRepository;
import com.buzzapp.safety_service.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final DeviceTokenRepository deviceTokenRepository;

    @PostMapping("/send")
    public ResponseEntity<NotificationResponse> send(
            @Valid @RequestBody SendNotificationRequest request,
            Authentication authentication) {
        Long schoolId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(notificationService.sendNotification(request, schoolId));
    }

    @PostMapping("/device-token")
    public ResponseEntity<Map<String, String>> registerDeviceToken(
            @Valid @RequestBody RegisterDeviceTokenRequest request,
            Authentication authentication) {
        Long schoolId = (Long) authentication.getPrincipal();
        deviceTokenRepository.deleteByToken(request.getToken());

        DeviceToken token = new DeviceToken();
        token.setRole(request.getRole().toUpperCase());
        token.setRecipientId(request.getRecipientId());
        token.setSchoolId(schoolId);
        token.setToken(request.getToken());
        token.setPlatform(request.getPlatform());
        token.setCreatedAt(LocalDateTime.now());
        deviceTokenRepository.save(token);
        return ResponseEntity.ok(Map.of("message", "Device registered"));
    }

    @GetMapping("/parent/{parentId}")
    public ResponseEntity<List<NotificationResponse>> getByParent(
            @PathVariable Long parentId,
            Authentication authentication) {
        Long schoolId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(notificationService.getNotificationsByParent(parentId, schoolId));
    }

    @GetMapping("/parent/{parentId}/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @PathVariable Long parentId,
            Authentication authentication) {
        Long schoolId = (Long) authentication.getPrincipal();
        long count = notificationService.getUnreadCount(parentId, schoolId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @GetMapping("/recipient/{role}/{recipientId}")
    public ResponseEntity<List<NotificationResponse>> getByRecipient(
            @PathVariable String role,
            @PathVariable Long recipientId,
            Authentication authentication) {
        Long schoolId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(notificationService.getNotificationsByRecipient(role, recipientId, schoolId));
    }

    @GetMapping("/recipient/{role}/{recipientId}/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCountByRecipient(
            @PathVariable String role,
            @PathVariable Long recipientId,
            Authentication authentication) {
        Long schoolId = (Long) authentication.getPrincipal();
        long count = notificationService.getUnreadCountByRecipient(role, recipientId, schoolId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Map<String, String>> markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return ResponseEntity.ok(Map.of("message", "Marked as read"));
    }

    @PutMapping("/parent/{parentId}/read-all")
    public ResponseEntity<Map<String, String>> markAllRead(
            @PathVariable Long parentId,
            Authentication authentication) {
        Long schoolId = (Long) authentication.getPrincipal();
        notificationService.markAllRead(parentId, schoolId);
        return ResponseEntity.ok(Map.of("message", "All marked as read"));
    }

    @PutMapping("/recipient/{role}/{recipientId}/read-all")
    public ResponseEntity<Map<String, String>> markAllReadByRecipient(
            @PathVariable String role,
            @PathVariable Long recipientId,
            Authentication authentication) {
        Long schoolId = (Long) authentication.getPrincipal();
        notificationService.markAllReadByRecipient(role, recipientId, schoolId);
        return ResponseEntity.ok(Map.of("message", "All marked as read"));
    }
}
