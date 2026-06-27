package com.buzzapp.safety_service.service;

import com.buzzapp.safety_service.dto.*;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NotificationService {

    public NotificationResponse send(NotificationSendRequest request, Long schoolId) {
        throw new UnsupportedOperationException("send() not yet implemented");
    }

    public List<NotificationResponse> getByParent(Long parentId, Long schoolId) {
        throw new UnsupportedOperationException("getByParent() not yet implemented");
    }
}