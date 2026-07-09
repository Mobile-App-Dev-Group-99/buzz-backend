package com.buzzapp.safety_service.repository;

import com.buzzapp.safety_service.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByParentIdAndSchoolIdOrderBySentAtDesc(Long parentId, Long schoolId);
}