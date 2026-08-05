package com.buzzapp.safety_service.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationResponse {
    private Long id;
    private Long parentId;
    private Long recipientId;
    private String recipientRole;
    private Long schoolId;
    private String message;
    private String type;
    private boolean isRead;
    private LocalDateTime sentAt;
}