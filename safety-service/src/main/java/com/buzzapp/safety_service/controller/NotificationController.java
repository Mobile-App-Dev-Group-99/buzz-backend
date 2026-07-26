package com.buzzapp.safety_service.controller;

import com.buzzapp.safety_service.dto.*;
import com.buzzapp.safety_service.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @GetMapping("/parent/{parentId}/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @PathVariable Long parentId,
            Authentication authentication) {
        Long schoolId = (Long) authentication.getPrincipal();
        long count = notificationService.getUnreadCount(parentId, schoolId);
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
}
