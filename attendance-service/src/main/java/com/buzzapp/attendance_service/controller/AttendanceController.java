package com.buzzapp.attendance_service.controller;

import com.buzzapp.attendance_service.dto.*;
import com.buzzapp.attendance_service.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/scan")
    public ResponseEntity<ScanResponse> scan(@RequestBody ScanRequest request,
                                             Authentication auth) {
        Long schoolId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(attendanceService.recordScan(request, schoolId));
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