package com.buzzapp.attendance_service.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "device_tokens")
@Data
public class DeviceToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20, nullable = false)
    private String role;

    @Column(nullable = false)
    private Long recipientId;

    @Column(nullable = false)
    private Long schoolId;

    @Column(nullable = false, length = 512)
    private String token;

    @Column(length = 20)
    private String platform;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
