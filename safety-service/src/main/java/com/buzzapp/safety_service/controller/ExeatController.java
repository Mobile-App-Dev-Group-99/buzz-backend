package com.buzzapp.safety_service.controller;

import com.buzzapp.safety_service.dto.*;
import com.buzzapp.safety_service.service.ExeatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exeat")
@RequiredArgsConstructor
public class ExeatController {

    private final ExeatService exeatService;

    @PostMapping("/create")
    public ResponseEntity<ExeatResponse> create(
            @Valid @RequestBody CreateExeatRequest request,
            Authentication authentication) {
        Long schoolId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(exeatService.createExeat(request, schoolId));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<ExeatResponse> approve(
            @PathVariable Long id,
            @RequestBody ApproveExeatRequest request,
            Authentication authentication) {
        Long schoolId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(exeatService.approveExeat(id, request, schoolId));
    }

    @PutMapping("/{id}/deny")
    public ResponseEntity<ExeatResponse> deny(
            @PathVariable Long id,
            @RequestBody ApproveExeatRequest request,
            Authentication authentication) {
        Long schoolId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(exeatService.denyExeat(id, request, schoolId));
    }

    @PutMapping("/{id}/return")
    public ResponseEntity<ExeatResponse> recordReturn(
            @PathVariable Long id,
            @RequestBody ReturnExeatRequest request,
            Authentication authentication) {
        Long schoolId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(exeatService.recordReturn(id, request, schoolId));
    }

    @GetMapping("/school")
    public ResponseEntity<List<ExeatResponse>> getSchoolExeats(Authentication authentication) {
        Long schoolId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(exeatService.getExeatsBySchoolResolved(schoolId));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<ExeatResponse>> getByStudent(
            @PathVariable Long studentId,
            Authentication authentication) {
        Long schoolId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(exeatService.getExeatsByStudentResolved(studentId, schoolId));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ExeatResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody ExeatStatusUpdateRequest request,
            Authentication authentication) {
        Long schoolId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(exeatService.updateExeatStatus(id, request, schoolId));
    }
}