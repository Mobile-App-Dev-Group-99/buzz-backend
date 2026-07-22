package com.buzzapp.attendance_service.repository;

import com.buzzapp.attendance_service.model.Exeat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExeatRepository extends JpaRepository<Exeat, Long> {
    List<Exeat> findBySchoolIdOrderByCreatedAtDesc(Long schoolId);
    List<Exeat> findByStudentIdAndSchoolIdOrderByCreatedAtDesc(Long studentId, Long schoolId);
    List<Exeat> findByStudentIdInAndSchoolIdOrderByCreatedAtDesc(List<Long> studentIds, Long schoolId);
}
