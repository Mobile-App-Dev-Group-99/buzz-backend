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
    public ResponseEntity<ExeatResponse> create(@RequestBody ExeatCreateRequest request) {
        return ResponseEntity.ok(exeatService.create(request, null));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<ExeatResponse> approve(@PathVariable Long id) {
        return ResponseEntity.ok(exeatService.approve(id, null, null));
    }

    @PutMapping("/{id}/return")
    public ResponseEntity<ExeatResponse> recordReturn(
            @PathVariable Long id,
            @RequestBody ExeatReturnRequest request) {
        return ResponseEntity.ok(exeatService.recordReturn(id, request, null));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<ExeatResponse>> getByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(exeatService.getByStudent(studentId, null));
    }
}