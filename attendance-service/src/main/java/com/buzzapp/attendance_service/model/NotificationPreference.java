package com.buzzapp.attendance_service.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "notification_preferences",
        uniqueConstraints = @UniqueConstraint(columnNames = {"role", "recipient_id", "category"}))
@Data
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20, nullable = false)
    private String role;

    @Column(nullable = false)
    private Long recipientId;

    @Column(length = 20, nullable = false)
    private String category;

    @Column(nullable = false)
    private boolean pushEnabled = true;

    @Column(nullable = false)
    private boolean emailEnabled = true;

    @Column(nullable = false)
    private boolean smsEnabled = false;
}
