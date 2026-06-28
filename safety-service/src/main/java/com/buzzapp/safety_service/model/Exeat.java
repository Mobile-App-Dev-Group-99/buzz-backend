package com.buzzapp.safety_service.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "exeats")
@Data
public class Exeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false)
    private Long schoolId;

    @Column(nullable = false)
    private String reason;

    private Long approvedBy;

    private LocalDateTime expectedReturn;

    private LocalDateTime actualReturn;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ExeatStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}