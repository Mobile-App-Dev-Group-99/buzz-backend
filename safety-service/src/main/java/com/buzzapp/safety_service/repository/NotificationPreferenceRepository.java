package com.buzzapp.safety_service.repository;

import com.buzzapp.safety_service.model.NotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {

    Optional<NotificationPreference> findByRoleAndRecipientIdAndCategory(String role, Long recipientId, String category);
}
