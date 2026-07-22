package com.buzzapp.attendance_service.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "teacher_classes")
@Data
public class TeacherClass {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "teacher_user_id")
    private Long teacherUserId;

    @Column(nullable = false, name = "class_name")
    private String className;

    @Column(nullable = false, name = "school_id")
    private Long schoolId;
}
