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

    @Column
    private String className;

    @Column
    private LocalDate dateOfBirth;

    @Column(nullable = false)
    private Long schoolId;

    @Column
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(unique = true)
    private Long userId;

    @Column
    private String photoUrl;

    @Column
    @Enumerated(EnumType.STRING)
    private StudentType studentType;
}
