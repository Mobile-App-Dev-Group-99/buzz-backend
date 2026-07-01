package com.buzzapp.attendance_service.repository;

import com.buzzapp.attendance_service.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findBySchoolId(Long schoolId);
    List<Student> findByClassNameAndSchoolId(String className, Long schoolId);
    long countBySchoolId(Long schoolId);
}