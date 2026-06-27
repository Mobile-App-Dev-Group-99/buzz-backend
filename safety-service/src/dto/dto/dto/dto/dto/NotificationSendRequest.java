package com.buzzapp.safety_service.dto;

import lombok.Data;

@Data
public class NotificationSendRequest {
    private Long parentId;
    private String message;
}