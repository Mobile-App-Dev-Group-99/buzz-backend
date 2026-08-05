package com.buzzapp.safety_service.controller;

import com.buzzapp.safety_service.service.ExeatService;
import com.buzzapp.safety_service.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class ReportsController {

    private final ExeatService exeatService;
    private final JwtService jwtService;

    @GetMapping("/exeats.csv")
    public ResponseEntity<String> exeatsReport(HttpServletRequest request,
                                               @RequestParam(required = false) String token) {
        Long schoolId = resolveSchoolId(request, token);
        if (schoolId == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"buzzapp-exeats-report.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(exeatService.getExeatsReportCsv(schoolId));
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
}
