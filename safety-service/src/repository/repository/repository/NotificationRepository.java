package com.buzzapp.safety_service.repository;

import com.buzzapp.safety_service.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}