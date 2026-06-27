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

    @Column
    private Long approvedBy;

    @Column
    private String reason;

    @Column
    private LocalDateTime expectedReturn;

    @Column
    private LocalDateTime actualReturn;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ExeatStatus status = ExeatStatus.PENDING;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}