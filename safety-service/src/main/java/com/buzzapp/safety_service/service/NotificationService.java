package com.buzzapp.safety_service.service;

import com.buzzapp.safety_service.dto.*;
import com.buzzapp.safety_service.model.Notification;
import com.buzzapp.safety_service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        notification.setType(request.getType());
        notification.setRead(false);
        notification.setSentAt(LocalDateTime.now());

        Notification saved = notificationRepository.save(notification);
        return toResponse(saved);
    }

    public List<NotificationResponse> getNotificationsByParent(Long parentId, Long schoolId) {
        return notificationRepository
                .findByParentIdAndSchoolIdOrderBySentAtDesc(parentId, schoolId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public long getUnreadCount(Long parentId, Long schoolId) {
        return notificationRepository.countUnread(parentId, schoolId);
    }

    @Transactional
    public void markRead(Long id) {
        notificationRepository.markReadById(id);
    }

    @Transactional
    public void markAllRead(Long parentId, Long schoolId) {
        notificationRepository.markAllRead(parentId, schoolId);
    }

    NotificationResponse notify(Long parentId, String message, Long schoolId) {
        return notify(parentId, message, schoolId, null);
    }

    NotificationResponse notify(Long parentId, String message, Long schoolId, String type) {
        SendNotificationRequest request = new SendNotificationRequest();
        request.setParentId(parentId);
        request.setMessage(message);
        request.setType(type);
        return sendNotification(request, schoolId);
    }

    private NotificationResponse toResponse(Notification n) {
        NotificationResponse response = new NotificationResponse();
        response.setId(n.getId());
        response.setParentId(n.getParentId());
        response.setSchoolId(n.getSchoolId());
        response.setMessage(n.getMessage());
        response.setType(n.getType());
        response.setRead(n.isRead());
        response.setSentAt(n.getSentAt());
        return response;
    }
}
