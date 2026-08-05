package com.buzzapp.attendance_service.service;

import com.buzzapp.attendance_service.dto.NotificationPreferenceItem;
import com.buzzapp.attendance_service.dto.NotificationPreferencesRequest;
import com.buzzapp.attendance_service.model.NotificationPreference;
import com.buzzapp.attendance_service.repository.NotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PreferenceService {

    public static final List<String> CATEGORIES = List.of("ATTENDANCE", "LATE", "ABSENT", "EXEAT");

    private final NotificationPreferenceRepository notificationPreferenceRepository;

    public List<NotificationPreferenceItem> getPreferences(String role, Long recipientId) {
        Map<String, NotificationPreference> byCategory = new LinkedHashMap<>();
        for (NotificationPreference pref : notificationPreferenceRepository.findByRoleAndRecipientId(
                normalize(role), recipientId)) {
            byCategory.put(pref.getCategory(), pref);
        }

        List<NotificationPreferenceItem> result = new ArrayList<>();
        for (String category : CATEGORIES) {
            NotificationPreference pref = byCategory.get(category);
            NotificationPreferenceItem item = new NotificationPreferenceItem();
            item.setCategory(category);
            if (pref != null) {
                item.setPushEnabled(pref.isPushEnabled());
                item.setEmailEnabled(pref.isEmailEnabled());
                item.setSmsEnabled(pref.isSmsEnabled());
            } else {
                item.setPushEnabled(true);
                item.setEmailEnabled(true);
                item.setSmsEnabled(false);
            }
            result.add(item);
        }
        return result;
    }

    @Transactional
    public List<NotificationPreferenceItem> updatePreferences(NotificationPreferencesRequest request) {
        String role = normalize(request.getRole());
        for (NotificationPreferenceItem item : request.getPreferences()) {
            String category = item.getCategory() == null ? "" : item.getCategory().toUpperCase();
            if (!CATEGORIES.contains(category)) continue;

            NotificationPreference pref = notificationPreferenceRepository
                    .findByRoleAndRecipientIdAndCategory(role, request.getRecipientId(), category)
                    .orElseGet(() -> {
                        NotificationPreference p = new NotificationPreference();
                        p.setRole(role);
                        p.setRecipientId(request.getRecipientId());
                        p.setCategory(category);
                        return p;
                    });
            pref.setPushEnabled(item.isPushEnabled());
            pref.setEmailEnabled(item.isEmailEnabled());
            pref.setSmsEnabled(item.isSmsEnabled());
            notificationPreferenceRepository.save(pref);
        }
        return getPreferences(role, request.getRecipientId());
    }

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
                .findByRoleAndRecipientIdAndCategory(normalize(role), recipientId, category)
                .orElse(null);
    }

    private String normalize(String role) {
        return role == null ? "PARENT" : role.toUpperCase();
    }
}
