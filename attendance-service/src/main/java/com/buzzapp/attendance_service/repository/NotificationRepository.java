package com.buzzapp.attendance_service.repository;

import com.buzzapp.attendance_service.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
