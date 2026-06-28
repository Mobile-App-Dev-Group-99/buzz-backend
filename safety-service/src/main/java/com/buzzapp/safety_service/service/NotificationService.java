package com.buzzapp.safety_service.service;

import com.buzzapp.safety_service.dto.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    public NotificationResponse sendNotification(SendNotificationRequest request, Long schoolId) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public List<NotificationResponse> getNotificationsByParent(Long parentId, Long schoolId) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}