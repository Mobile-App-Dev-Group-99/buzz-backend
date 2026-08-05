package com.buzzapp.attendance_service.repository;

import com.buzzapp.attendance_service.model.NotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {

    List<NotificationPreference> findByRoleAndRecipientId(String role, Long recipientId);

    Optional<NotificationPreference> findByRoleAndRecipientIdAndCategory(String role, Long recipientId, String category);
}
