package com.buzzapp.attendance_service.repository;

import com.buzzapp.attendance_service.model.AttendanceEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceEventRepository extends JpaRepository<AttendanceEvent, Long> {
}