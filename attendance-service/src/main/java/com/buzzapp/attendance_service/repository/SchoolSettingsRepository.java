package com.buzzapp.attendance_service.repository;

import com.buzzapp.attendance_service.model.SchoolSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchoolSettingsRepository extends JpaRepository<SchoolSettings, Long> {
}
