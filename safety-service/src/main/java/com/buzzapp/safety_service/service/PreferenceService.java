package com.buzzapp.safety_service.service;

import com.buzzapp.safety_service.model.NotificationPreference;
import com.buzzapp.safety_service.repository.NotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PreferenceService {

    public static final String EXEAT_CATEGORY = "EXEAT";

    private final NotificationPreferenceRepository notificationPreferenceRepository;

    public boolean isPushEnabled(String role, Long recipientId, String category) {
        NotificationPreference pref = findBy(role, recipientId, category);
        return pref == null || pref.isPushEnabled();
    }

    public boolean isEmailEnabled(String role, Long recipientId, String category) {
        NotificationPreference pref = findBy(role, recipientId, category);
        return pref == null || pref.isEmailEnabled();
    }

    public boolean isSmsEnabled(String role, Long recipientId, String category) {
        NotificationPreference pref = findBy(role, recipientId, category);
        return pref != null && pref.isSmsEnabled();
    }

    private NotificationPreference findBy(String role, Long recipientId, String category) {
        return notificationPreferenceRepository
                .findByRoleAndRecipientIdAndCategory(role.toUpperCase(), recipientId, category)
                .orElse(null);
    }
}
