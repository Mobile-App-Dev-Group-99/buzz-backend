package com.buzzapp.safety_service.service;

import com.buzzapp.safety_service.model.SchoolSettings;
import com.buzzapp.safety_service.repository.SchoolSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private final SchoolSettingsRepository schoolSettingsRepository;

    public boolean isExeatAlertsEnabled(Long schoolId) {
        SchoolSettings settings = schoolSettingsRepository.findById(schoolId).orElse(null);
        return settings == null || settings.isAlertsExeat();
    }
}
