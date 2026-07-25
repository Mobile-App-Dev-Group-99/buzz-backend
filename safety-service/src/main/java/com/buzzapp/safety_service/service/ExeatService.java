package com.buzzapp.safety_service.service;

import com.buzzapp.safety_service.dto.*;
import com.buzzapp.safety_service.model.Exeat;
import com.buzzapp.safety_service.model.ExeatStatus;
import com.buzzapp.safety_service.model.StudentParent;
import com.buzzapp.safety_service.repository.ExeatRepository;
import com.buzzapp.safety_service.repository.StudentParentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExeatService {

    private final ExeatRepository exeatRepository;
    private final StudentParentRepository studentParentRepository;
    private final NotificationService notificationService;

    public ExeatResponse createExeat(CreateExeatRequest request, Long schoolId) {
        Exeat exeat = new Exeat();
        exeat.setStudentId(request.getStudentId());
        exeat.setSchoolId(schoolId);
        exeat.setReason(request.getReason());
        exeat.setExpectedReturn(request.getExpectedReturn());
        exeat.setStatus(ExeatStatus.PENDING);
        exeat.setCreatedAt(LocalDateTime.now());

        return toResponse(exeatRepository.save(exeat));
    }

    public ExeatResponse approveExeat(Long exeatId, ApproveExeatRequest request, Long schoolId) {
        Exeat exeat = getOwnedExeat(exeatId, schoolId);

        if (exeat.getStatus() != ExeatStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Exeat is " + exeat.getStatus() + " and cannot be approved");
        }

        exeat.setStatus(ExeatStatus.APPROVED);
        exeat.setApprovedBy(request.getApprovedBy());
        Exeat saved = exeatRepository.save(exeat);

        notifyParents(exeat.getStudentId(),
                "Exeat approved for student #" + exeat.getStudentId(), schoolId);

        return toResponse(saved);
    }

    public ExeatResponse denyExeat(Long exeatId, ApproveExeatRequest request, Long schoolId) {
        Exeat exeat = getOwnedExeat(exeatId, schoolId);

        if (exeat.getStatus() != ExeatStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Exeat is " + exeat.getStatus() + " and cannot be denied");
        }

        exeat.setStatus(ExeatStatus.DENIED);
        exeat.setApprovedBy(request.getApprovedBy());

        Exeat saved = exeatRepository.save(exeat);

        notifyParents(exeat.getStudentId(),
                "Exeat denied for student #" + exeat.getStudentId(), schoolId);

        return toResponse(saved);
    }

    public ExeatResponse recordReturn(Long exeatId, ReturnExeatRequest request, Long schoolId) {
        Exeat exeat = getOwnedExeat(exeatId, schoolId);

        if (exeat.getStatus() != ExeatStatus.APPROVED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Exeat must be APPROVED before a return can be recorded (currently " + exeat.getStatus() + ")");
        }

        exeat.setActualReturn(request.getActualReturn() != null ? request.getActualReturn() : LocalDateTime.now());
        exeat.setStatus(ExeatStatus.RETURNED);
        Exeat saved = exeatRepository.save(exeat);

        notifyParents(exeat.getStudentId(),
                "Student #" + exeat.getStudentId() + " has returned from exeat", schoolId);

        return toResponse(saved);
    }

    public List<ExeatResponse> getExeatsByStudent(Long studentId, Long schoolId) {
        return exeatRepository
                .findByStudentIdAndSchoolIdOrderByCreatedAtDesc(studentId, schoolId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ExeatResponse> getExeatsBySchool(Long schoolId) {
        return exeatRepository
                .findBySchoolIdOrderByCreatedAtDesc(schoolId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ExeatResponse updateExeatStatus(Long exeatId, ExeatStatusUpdateRequest request, Long schoolId) {
        Exeat exeat = getOwnedExeat(exeatId, schoolId);

        ExeatStatus newStatus;
        try {
            newStatus = ExeatStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid status: " + request.getStatus());
        }

        if (exeat.getStatus() != ExeatStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Exeat is " + exeat.getStatus() + " and cannot be updated");
        }

        exeat.setStatus(newStatus);
        Exeat saved = exeatRepository.save(exeat);

        notifyParents(exeat.getStudentId(),
                "Exeat status updated to " + newStatus + " for student #" + exeat.getStudentId(), schoolId);

        return toResponse(saved);
    }

    private void notifyParents(Long studentId, String message, Long schoolId) {
        List<StudentParent> links = studentParentRepository.findByIdStudentId(studentId);
        for (StudentParent link : links) {
            Long parentId = link.getId().getParentId();
            try {
                notificationService.notify(parentId, message, schoolId);
            } catch (Exception ignored) {
            }
        }
    }

    private Exeat getOwnedExeat(Long exeatId, Long schoolId) {
        Exeat exeat = exeatRepository.findById(exeatId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exeat not found"));

        if (!exeat.getSchoolId().equals(schoolId)) {
            // Treat cross-school access as "not found" — never leak existence across tenants.
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Exeat not found");
        }

        return exeat;
    }

    private ExeatResponse toResponse(Exeat e) {
        ExeatResponse response = new ExeatResponse();
        response.setId(e.getId());
        response.setStudentId(e.getStudentId());
        response.setSchoolId(e.getSchoolId());
        response.setReason(e.getReason());
        response.setApprovedBy(e.getApprovedBy());
        response.setExpectedReturn(e.getExpectedReturn());
        response.setActualReturn(e.getActualReturn());
        response.setStatus(e.getStatus());
        response.setCreatedAt(e.getCreatedAt());
        return response;
    }
}