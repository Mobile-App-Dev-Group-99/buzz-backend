package com.buzzapp.attendance_service.controller;

import com.buzzapp.attendance_service.dto.*;
import com.buzzapp.attendance_service.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/scan")
    public ResponseEntity<ScanResponse> scan(@Valid @RequestBody ScanRequest request,
                                             Authentication auth) {
        Long schoolId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(attendanceService.recordScan(request, schoolId));
    }

    @PostMapping("/manual")
    public ResponseEntity<?> manualMark(@Valid @RequestBody ManualAttendanceRequest request,
                                        Authentication auth) {
        Long schoolId = (Long) auth.getPrincipal();
        String email = (String) auth.getCredentials();
        try {
            return ResponseEntity.ok(attendanceService.markManualAttendance(request, schoolId, email));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/self-checkin")
    public ResponseEntity<?> selfCheckIn(Authentication auth) {
        Long schoolId = (Long) auth.getPrincipal();
        String email = (String) auth.getCredentials();
        try {
            return ResponseEntity.ok(attendanceService.selfCheckIn(email, schoolId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/summary/today")
    public ResponseEntity<TodaySummaryResponse> todaySummary(Authentication auth) {
        Long schoolId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(attendanceService.getTodaySummary(schoolId));
    }

    @GetMapping("/live")
    public ResponseEntity<List<LiveFeedEntry>> liveFeed(Authentication auth) {
        Long schoolId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(attendanceService.getLiveFeed(schoolId));
    }

    @GetMapping("/classes/today")
    public ResponseEntity<List<ClassAttendanceResponse>> classesToday(Authentication auth) {
        Long schoolId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(attendanceService.getClassesToday(schoolId));
    }

    @GetMapping("/weekly")
    public ResponseEntity<WeeklyAttendanceResponse> weekly(Authentication auth) {
        Long schoolId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(attendanceService.getWeeklyRates(schoolId));
    }

    @GetMapping("/class/{className}/today")
    public ResponseEntity<List<StudentAttendanceEntry>> classToday(@PathVariable String className,
                                                                   Authentication auth) {
        Long schoolId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(attendanceService.getClassToday(className, schoolId));
    }

    @GetMapping("/class/{className}/roster")
    public ResponseEntity<List<Map<String, Object>>> classRoster(@PathVariable String className,
                                                                 Authentication auth) {
        Long schoolId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(attendanceService.getClassRoster(className, schoolId));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<StudentAttendanceEntry>> studentHistory(@PathVariable Long studentId,
                                                                       Authentication auth) {
        Long schoolId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(attendanceService.getStudentHistory(studentId, schoolId));
    }

    @GetMapping("/student/{studentId}/term")
    public ResponseEntity<StudentTermSummaryResponse> studentTerm(@PathVariable Long studentId,
                                                                  Authentication auth) {
        Long schoolId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(attendanceService.getStudentTermSummary(studentId, schoolId));
    }

    @GetMapping("/student/{studentId}/week")
    public ResponseEntity<WeeklyCalendarResponse> studentWeek(@PathVariable Long studentId,
                                                              Authentication auth) {
        Long schoolId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(attendanceService.getStudentWeek(studentId, schoolId));
    }
}