package com.buzzapp.attendance_service.repository;

import com.buzzapp.attendance_service.model.BiometricTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BiometricTemplateRepository extends JpaRepository<BiometricTemplate, Long> {
    Optional<BiometricTemplate> findBySlotId(Integer slotId);
    Optional<BiometricTemplate> findByStudentId(Long studentId);
}