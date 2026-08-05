package com.buzzapp.attendance_service.controller;

import com.buzzapp.attendance_service.service.JwtService;
import com.buzzapp.attendance_service.service.ReportsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class ReportsController {

    private final ReportsService reportsService;
    private final JwtService jwtService;

    @GetMapping("/attendance.csv")
    public ResponseEntity<String> attendanceReport(HttpServletRequest request,
                                                   @RequestParam(required = false) String token) {
        Long schoolId = resolveSchoolId(request, token);
        if (schoolId == null) return unauthorized();
        return csv("buzzapp-attendance-report.csv", reportsService.getAttendanceReportCsv(schoolId));
    }

    @GetMapping("/students.csv")
    public ResponseEntity<String> studentsReport(HttpServletRequest request,
                                                 @RequestParam(required = false) String token) {
        Long schoolId = resolveSchoolId(request, token);
        if (schoolId == null) return unauthorized();
        return csv("buzzapp-students-report.csv", reportsService.getStudentsReportCsv(schoolId));
    }

    private Long resolveSchoolId(HttpServletRequest request, String token) {
        String raw = request.getHeader("Authorization");
        if (raw != null && raw.startsWith("Bearer ")) {
            token = raw.substring(7);
        }
        if (token == null || !jwtService.isValid(token)) return null;
        String role = jwtService.extractRole(token);
        if (!"ADMIN".equalsIgnoreCase(role)) return null;
        return jwtService.extractSchoolId(token);
    }

    private ResponseEntity<String> unauthorized() {
        return ResponseEntity.status(401).body("Unauthorized");
    }

    private ResponseEntity<String> csv(String filename, String content) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(content);
    }
}
