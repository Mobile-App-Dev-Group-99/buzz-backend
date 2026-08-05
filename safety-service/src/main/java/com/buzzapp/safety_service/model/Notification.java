package com.buzzapp.safety_service.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long parentId;

    @Column
    private Long recipientId;

    @Column(length = 20)
    private String recipientRole;

    @Column(nullable = false)
    private Long schoolId;

    @Column(nullable = false)
    private String message;

    private String type;

    @Column(nullable = false)
    private boolean isRead;

    @Column(nullable = false)
    private LocalDateTime sentAt;
}