package com.buzzapp.safety_service.repository;

import com.buzzapp.safety_service.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findByIdIn(List<Long> ids);
}
