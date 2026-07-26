package com.buzzapp.attendance_service.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_events")
@Data
public class AttendanceEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ScanType scanType;

    @Column(nullable = false)
    private LocalDateTime  scannedAt;

    @Column(nullable = false, name = "is_late")
    private boolean isLate;

    @Column(length = 50)
    private String gate;

    @Column(nullable = false)
    private Long schoolId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private AttendanceStatus status;
}
