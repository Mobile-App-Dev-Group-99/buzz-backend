package com.buzzapp.attendance_service.repository;

import com.buzzapp.attendance_service.model.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findBySchoolId(Long schoolId);
    Page<Student> findBySchoolId(Long schoolId, Pageable pageable);
    List<Student> findByClassNameAndSchoolId(String className, Long schoolId);
    long countBySchoolId(Long schoolId);
    Optional<Student> findByUserId(Long userId);
}