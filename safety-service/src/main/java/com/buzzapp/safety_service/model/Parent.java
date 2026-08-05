package com.buzzapp.safety_service.model;

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

    private String phone;

    private String email;

    @Column(unique = true)
    private Long userId;
}
