package com.buzzapp.attendance_service.dto;

import lombok.Data;

import java.util.List;

@Data
public class NotificationPreferencesRequest {
    private String role;
    private Long recipientId;
    private List<NotificationPreferenceItem> preferences;
}
