package com.buzzapp.attendance_service.repository;

import com.buzzapp.attendance_service.model.BiometricTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BiometricTemplateRepository extends JpaRepository<BiometricTemplate, Long> {
}