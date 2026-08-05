package com.buzzapp.safety_service.service;

import com.buzzapp.safety_service.dto.*;
import com.buzzapp.safety_service.model.Notification;
import com.buzzapp.safety_service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final PushNotificationService pushNotificationService;

    public NotificationResponse sendNotification(SendNotificationRequest request, Long schoolId) {
        return sendNotificationInternal(request, schoolId, true);
    }

    private NotificationResponse sendNotificationInternal(SendNotificationRequest request, Long schoolId, boolean pushEnabled) {
        Long recipientId = request.getRecipientId() != null ? request.getRecipientId() : request.getParentId();
        String recipientRole = request.getRecipientRole() != null
                ? request.getRecipientRole().toUpperCase()
                : "PARENT";

        Notification notification = new Notification();
        notification.setParentId(request.getParentId());
        notification.setRecipientId(recipientId);
        notification.setRecipientRole(recipientRole);
        notification.setSchoolId(schoolId);
        notification.setMessage(request.getMessage());
        notification.setType(request.getType());
        notification.setRead(false);
        notification.setSentAt(LocalDateTime.now());

        Notification saved = notificationRepository.save(notification);
        if (pushEnabled) {
            pushNotificationService.pushToRecipient(
                    recipientRole, recipientId, schoolId, "BuzzApp", saved.getMessage());
        }
        return toResponse(saved);
    }

    public List<NotificationResponse> getNotificationsByParent(Long parentId, Long schoolId) {
        return notificationRepository
                .findByParentIdAndSchoolIdOrderBySentAtDesc(parentId, schoolId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<NotificationResponse> getNotificationsByRecipient(String role, Long recipientId, Long schoolId) {
        return notificationRepository
                .findByRecipientRoleAndRecipientIdAndSchoolIdOrderBySentAtDesc(
                        role.toUpperCase(), recipientId, schoolId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public long getUnreadCount(Long parentId, Long schoolId) {
        return notificationRepository.countUnread(parentId, schoolId);
    }

    public long getUnreadCountByRecipient(String role, Long recipientId, Long schoolId) {
        return notificationRepository.countUnreadByRecipient(role.toUpperCase(), recipientId, schoolId);
    }

    @Transactional
    public void markRead(Long id) {
        notificationRepository.markReadById(id);
    }

    @Transactional
    public void markAllRead(Long parentId, Long schoolId) {
        notificationRepository.markAllRead(parentId, schoolId);
    }

    @Transactional
    public void markAllReadByRecipient(String role, Long recipientId, Long schoolId) {
        notificationRepository.markAllReadByRecipient(role.toUpperCase(), recipientId, schoolId);
    }

    NotificationResponse notify(Long parentId, String message, Long schoolId) {
        return notify(parentId, message, schoolId, null);
    }

    NotificationResponse notify(Long parentId, String message, Long schoolId, String type) {
        return notify(parentId, message, schoolId, type, true);
    }

    NotificationResponse notify(Long parentId, String message, Long schoolId, String type, boolean pushEnabled) {
        SendNotificationRequest request = new SendNotificationRequest();
        request.setParentId(parentId);
        request.setRecipientId(parentId);
        request.setRecipientRole("PARENT");
        request.setMessage(message);
        request.setType(type);
        return sendNotificationInternal(request, schoolId, pushEnabled);
    }

    private NotificationResponse toResponse(Notification n) {
        NotificationResponse response = new NotificationResponse();
        response.setId(n.getId());
        response.setParentId(n.getParentId());
        response.setRecipientId(n.getRecipientId());
        response.setRecipientRole(n.getRecipientRole());
        response.setSchoolId(n.getSchoolId());
        response.setMessage(n.getMessage());
        response.setType(n.getType());
        response.setRead(n.isRead());
        response.setSentAt(n.getSentAt());
        return response;
    }
}
