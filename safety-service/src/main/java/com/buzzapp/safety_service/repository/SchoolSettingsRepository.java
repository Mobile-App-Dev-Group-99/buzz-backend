package com.buzzapp.safety_service.repository;

import com.buzzapp.safety_service.model.SchoolSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchoolSettingsRepository extends JpaRepository<SchoolSettings, Long> {
}
