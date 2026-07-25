package com.buzzapp.attendance_service.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "biometric_templates")
public class BiometricTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false)
    private String template;

    private Integer slotId;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
