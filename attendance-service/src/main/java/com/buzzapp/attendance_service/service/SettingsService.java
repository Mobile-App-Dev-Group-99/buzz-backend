package com.buzzapp.attendance_service.service;

import com.buzzapp.attendance_service.dto.SchoolSettingsRequest;
import com.buzzapp.attendance_service.dto.SchoolSettingsResponse;
import com.buzzapp.attendance_service.model.SchoolSettings;
import com.buzzapp.attendance_service.repository.SchoolSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private final SchoolSettingsRepository schoolSettingsRepository;

    @Value("${attendance.arrival.cutoff}")
    private String defaultArrivalCutoff;

    public SchoolSettingsResponse getSettings(Long schoolId) {
        SchoolSettings settings = findOrCreate(schoolId);
        return toResponse(settings);
    }

    @Transactional
    public SchoolSettingsResponse updateSettings(Long schoolId, SchoolSettingsRequest request) {
        SchoolSettings settings = findOrCreate(schoolId);

        if (request.getArrivalCutoff() != null && !request.getArrivalCutoff().isBlank()) {
            settings.setArrivalCutoff(request.getArrivalCutoff().trim());
        }
        if (request.getAlertsAbsent() != null) settings.setAlertsAbsent(request.getAlertsAbsent());
        if (request.getAlertsLate() != null) settings.setAlertsLate(request.getAlertsLate());
        if (request.getAlertsExeat() != null) settings.setAlertsExeat(request.getAlertsExeat());

        return toResponse(schoolSettingsRepository.save(settings));
    }

    public String getArrivalCutoff(Long schoolId) {
        SchoolSettings settings = schoolSettingsRepository.findById(schoolId).orElse(null);
        if (settings != null && settings.getArrivalCutoff() != null && !settings.getArrivalCutoff().isBlank()) {
            return settings.getArrivalCutoff();
        }
        return defaultArrivalCutoff;
    }

    private SchoolSettings findOrCreate(Long schoolId) {
        SchoolSettings settings = schoolSettingsRepository.findById(schoolId).orElse(null);
        if (settings == null) {
            settings = new SchoolSettings();
            settings.setSchoolId(schoolId);
            settings.setArrivalCutoff(defaultArrivalCutoff);
            settings.setAlertsAbsent(true);
            settings.setAlertsLate(true);
            settings.setAlertsExeat(true);
            settings = schoolSettingsRepository.save(settings);
        }
        return settings;
    }

    private SchoolSettingsResponse toResponse(SchoolSettings settings) {
        SchoolSettingsResponse response = new SchoolSettingsResponse();
        response.setSchoolId(settings.getSchoolId());
        response.setArrivalCutoff(settings.getArrivalCutoff() != null ? settings.getArrivalCutoff() : defaultArrivalCutoff);
        response.setAlertsAbsent(settings.isAlertsAbsent());
        response.setAlertsLate(settings.isAlertsLate());
        response.setAlertsExeat(settings.isAlertsExeat());
        return response;
    }
}
