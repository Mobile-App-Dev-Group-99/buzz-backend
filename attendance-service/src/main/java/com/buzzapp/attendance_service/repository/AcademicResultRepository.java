package com.buzzapp.attendance_service.repository;

import com.buzzapp.attendance_service.model.AcademicResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AcademicResultRepository extends JpaRepository<AcademicResult, Long> {
    List<AcademicResult> findByStudentIdOrderByYearDescTermAscSubjectAsc(Long studentId);
    List<AcademicResult> findByStudentIdAndTermOrderBySubjectAsc(Long studentId, String term);
}
