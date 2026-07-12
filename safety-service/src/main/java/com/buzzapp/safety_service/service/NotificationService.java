package com.buzzapp.safety_service.service;

import com.buzzapp.safety_service.dto.*;
import com.buzzapp.safety_service.model.Notification;
import com.buzzapp.safety_service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationResponse sendNotification(SendNotificationRequest request, Long schoolId) {
        Notification notification = new Notification();
        notification.setParentId(request.getParentId());
        notification.setSchoolId(schoolId);
        notification.setMessage(request.getMessage());
        notification.setRead(false);
        notification.setSentAt(LocalDateTime.now());

        Notification saved = notificationRepository.save(notification);

        // TODO: fire FCM push here once the Firebase service account JSON is available.
        // For now this only persists the notification record.

        return toResponse(saved);
    }

    public List<NotificationResponse> getNotificationsByParent(Long parentId, Long schoolId) {
        return notificationRepository
                .findByParentIdAndSchoolIdOrderBySentAtDesc(parentId, schoolId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Package-private helper so ExeatService can trigger a notification
    // without building a SendNotificationRequest by hand each time.
    NotificationResponse notify(Long parentId, String message, Long schoolId) {
        SendNotificationRequest request = new SendNotificationRequest();
        request.setParentId(parentId);
        request.setMessage(message);
        return sendNotification(request, schoolId);
    }

    private NotificationResponse toResponse(Notification n) {
        NotificationResponse response = new NotificationResponse();
        response.setId(n.getId());
        response.setParentId(n.getParentId());
        response.setSchoolId(n.getSchoolId());
        response.setMessage(n.getMessage());
        response.setRead(n.isRead());
        response.setSentAt(n.getSentAt());
        return response;
    }
}