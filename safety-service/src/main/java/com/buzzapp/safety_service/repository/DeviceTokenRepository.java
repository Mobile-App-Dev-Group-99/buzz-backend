package com.buzzapp.safety_service.repository;

import com.buzzapp.safety_service.model.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {
    List<DeviceToken> findByRoleAndRecipientIdAndSchoolId(String role, Long recipientId, Long schoolId);
    Optional<DeviceToken> findByToken(String token);
    void deleteByToken(String token);
}
