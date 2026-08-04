package com.buzzapp.attendance_service.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "academic_results")
@Data
public class AcademicResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "submitted_by", nullable = false)
    private Long submittedBy;

    @Column(nullable = false, length = 100)
    private String subject;

    @Column(precision = 5, scale = 2)
    private BigDecimal score;

    @Column(length = 5)
    private String grade;

    @Column(length = 20)
    private String term;

    @Column
    private Integer year;

    @Column(name = "teacher_remark", columnDefinition = "TEXT")
    private String teacherRemark;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
