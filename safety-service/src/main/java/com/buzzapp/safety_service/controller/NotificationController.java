package com.buzzapp.safety_service.controller;

import com.buzzapp.safety_service.dto.*;
import com.buzzapp.safety_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    public ResponseEntity<NotificationResponse> send(@RequestBody SendNotificationRequest request) {
        return ResponseEntity.ok(notificationService.sendNotification(request, null));
    }

    @GetMapping("/parent/{parentId}")
    public ResponseEntity<List<NotificationResponse>> getByParent(@PathVariable Long parentId) {
        return ResponseEntity.ok(notificationService.getNotificationsByParent(parentId, null));
    }
}