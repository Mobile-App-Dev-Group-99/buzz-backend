package com.buzzapp.attendance_service.dto;

import lombok.Data;

@Data
public class NotificationPreferenceItem {
    private String category;
    private boolean pushEnabled;
    private boolean emailEnabled;
    private boolean smsEnabled;
}
