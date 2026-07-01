package com.buzzapp.attendance_service.repository;

import com.buzzapp.attendance_service.model.AttendanceEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AttendanceEventRepository extends JpaRepository<AttendanceEvent, Long> {
    List<AttendanceEvent> findBySchoolIdAndScannedAtBetween(
            Long schoolId, LocalDateTime start, LocalDateTime end);

    List<AttendanceEvent> findBySchoolIdAndScannedAtBetweenOrderByScannedAtDesc(
            Long schoolId, LocalDateTime start, LocalDateTime end);

    List<AttendanceEvent> findByStudentIdAndSchoolIdAndScannedAtBetweenOrderByScannedAtDesc(
            Long studentId, Long schoolId, LocalDateTime start, LocalDateTime end);

    List<AttendanceEvent> findByStudentIdAndSchoolIdOrderByScannedAtDesc(
            Long studentId, Long schoolId);

    List<AttendanceEvent> findByStudentIdAndSchoolIdAndScannedAtBetween(
            Long studentId, Long schoolId, LocalDateTime start, LocalDateTime end);
}