package com.buzzapp.auth_service.repository;

import com.buzzapp.auth_service.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {
}
