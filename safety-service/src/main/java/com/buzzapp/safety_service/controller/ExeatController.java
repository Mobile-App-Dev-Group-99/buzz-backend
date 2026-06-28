package com.buzzapp.safety_service.controller;

import com.buzzapp.safety_service.dto.*;
import com.buzzapp.safety_service.service.ExeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exeat")
@RequiredArgsConstructor
public class ExeatController {

    private final ExeatService exeatService;

    @PostMapping("/create")
    public ResponseEntity<ExeatResponse> create(@RequestBody CreateExeatRequest request) {
        return ResponseEntity.ok(exeatService.createExeat(request, null));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<ExeatResponse> approve(
            @PathVariable Long id,
            @RequestBody ApproveExeatRequest request) {
        return ResponseEntity.ok(exeatService.approveExeat(id, request, null));
    }

    @PutMapping("/{id}/return")
    public ResponseEntity<ExeatResponse> recordReturn(
            @PathVariable Long id,
            @RequestBody ReturnExeatRequest request) {
        return ResponseEntity.ok(exeatService.recordReturn(id, request, null));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<ExeatResponse>> getByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(exeatService.getExeatsByStudent(studentId, null));
    }
}