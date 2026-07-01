package com.buzzapp.attendance_service.controller;

import com.buzzapp.attendance_service.dto.*;
import com.buzzapp.attendance_service.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/scan")
    public ResponseEntity<ScanResponse> scan(@RequestBody ScanRequest request) {
        return ResponseEntity.ok(attendanceService.recordScan(request, null));
    }

    @GetMapping("/summary/today")
    public ResponseEntity<TodaySummaryResponse> todaySummary() {
        return ResponseEntity.ok(attendanceService.getTodaySummary(null));
    }

    @GetMapping("/live")
    public ResponseEntity<List<LiveFeedEntry>> liveFeed() {
        return ResponseEntity.ok(attendanceService.getLiveFeed(null));
    }

    @GetMapping("/classes/today")
    public ResponseEntity<List<ClassAttendanceResponse>> classesToday() {
        return ResponseEntity.ok(attendanceService.getClassesToday(null));
    }

    @GetMapping("/weekly")
    public ResponseEntity<WeeklyAttendanceResponse> weekly() {
        return ResponseEntity.ok(attendanceService.getWeeklyRates(null));
    }

    @GetMapping("/class/{classId}/today")
    public ResponseEntity<List<StudentAttendanceEntry>> classToday(@PathVariable Long classId) {
        return ResponseEntity.ok(attendanceService.getClassToday(String.valueOf(classId), null));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<StudentAttendanceEntry>> studentHistory(@PathVariable Long studentId) {
        return ResponseEntity.ok(attendanceService.getStudentHistory(studentId, null));
    }

    @GetMapping("/student/{studentId}/term")
    public ResponseEntity<StudentTermSummaryResponse> studentTerm(@PathVariable Long studentId) {
        return ResponseEntity.ok(attendanceService.getStudentTermSummary(studentId, null));
    }

    @GetMapping("/student/{studentId}/week")
    public ResponseEntity<WeeklyCalendarResponse> studentWeek(@PathVariable Long studentId) {
        return ResponseEntity.ok(attendanceService.getStudentWeek(studentId, null));
    }

    @GetMapping("/class/{className}/today")
    public ResponseEntity<List<StudentAttendanceEntry>> classToday(@PathVariable String className) {
        return ResponseEntity.ok(attendanceService.getClassToday(className, null));
    }
}