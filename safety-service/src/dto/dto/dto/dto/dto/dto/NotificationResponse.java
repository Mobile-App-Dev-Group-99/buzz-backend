package com.buzzapp.safety_service.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationResponse {
    private Long id;
    private Long parentId;
    private String message;
    private LocalDateTime sentAt;
    private boolean isRead;
}