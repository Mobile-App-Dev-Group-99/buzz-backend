package com.buzzapp.attendance_service.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "parents")
@Data
public class Parent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private int phone;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, unique = true)
    private Long userId;
}
