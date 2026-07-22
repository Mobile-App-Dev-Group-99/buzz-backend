package com.buzzapp.safety_service.repository;

import com.buzzapp.safety_service.model.Exeat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExeatRepository extends JpaRepository<Exeat, Long> {
    List<Exeat> findByStudentIdAndSchoolIdOrderByCreatedAtDesc(Long studentId, Long schoolId);
    List<Exeat> findBySchoolIdOrderByCreatedAtDesc(Long schoolId);
}