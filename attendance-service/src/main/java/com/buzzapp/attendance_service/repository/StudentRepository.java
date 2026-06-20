package com.buzzapp.attendance_service.repository;

import com.buzzapp.attendance_service.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {
}