package com.buzzapp.attendance_service.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

@Entity
@Table(name = "students")
@Data
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String className;

    @Column
    private LocalDate dateOfBirth;

    @Column(nullable = false)
    private Long schoolId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false)
    private String photoUrl;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StudentType studentType;
}
