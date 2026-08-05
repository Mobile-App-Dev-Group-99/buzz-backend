package com.buzzapp.attendance_service.repository;

import com.buzzapp.attendance_service.model.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {
    List<DeviceToken> findByRoleAndRecipientIdAndSchoolId(String role, Long recipientId, Long schoolId);
}
