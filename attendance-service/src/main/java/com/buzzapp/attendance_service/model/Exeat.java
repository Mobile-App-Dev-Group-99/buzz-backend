package com.buzzapp.attendance_service.model;

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

    private String notes;

    private Long approvedBy;

    private LocalDateTime expectedReturn;

    private LocalDateTime actualReturn;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ExeatStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
