package com.buzzapp.safety_service.service;

import com.buzzapp.safety_service.dto.*;
import com.buzzapp.safety_service.model.Exeat;
import com.buzzapp.safety_service.model.ExeatStatus;
import com.buzzapp.safety_service.model.Parent;
import com.buzzapp.safety_service.model.Student;
import com.buzzapp.safety_service.model.StudentParent;
import com.buzzapp.safety_service.model.User;
import com.buzzapp.safety_service.repository.ExeatRepository;
import com.buzzapp.safety_service.repository.ParentRepository;
import com.buzzapp.safety_service.repository.StudentParentRepository;
import com.buzzapp.safety_service.repository.StudentRepository;
import com.buzzapp.safety_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExeatService {

    private final ExeatRepository exeatRepository;
    private final StudentParentRepository studentParentRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final ParentRepository parentRepository;
    private final JavaMailSender mailSender;
    private final SettingsService settingsService;
    private final PreferenceService preferenceService;
    private final SmsService smsService;

    @Value("${app.school.name:BuzzApp}")
    private String schoolName;

    public ExeatResponse createExeat(CreateExeatRequest request, Long schoolId) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student not found"));

        if (!student.getSchoolId().equals(schoolId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student does not belong to this school");
        }

        Exeat exeat = new Exeat();
        exeat.setStudentId(request.getStudentId());
        exeat.setSchoolId(schoolId);
        exeat.setReason(request.getReason());
        exeat.setNotes(request.getNotes());
        exeat.setExpectedReturn(request.getExpectedReturn());
        exeat.setStatus(ExeatStatus.PENDING);
        exeat.setCreatedAt(LocalDateTime.now());

        Exeat saved = exeatRepository.save(exeat);

        String studentName = student.getFirstName() + " " + student.getLastName();
        notifyParents(student.getId(),
                studentName + " has submitted an exeat request: " + exeat.getReason(), schoolId, "EXEAT_CREATED");

        return toResponse(saved);
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

        String studentName = resolveStudentName(exeat.getStudentId());
        notifyParents(exeat.getStudentId(),
                studentName + "'s exeat has been approved", schoolId, "EXEAT_APPROVED");

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

        String studentName = resolveStudentName(exeat.getStudentId());
        notifyParents(exeat.getStudentId(),
                studentName + "'s exeat has been denied", schoolId, "EXEAT_DENIED");

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

        String studentName = resolveStudentName(exeat.getStudentId());
        notifyParents(exeat.getStudentId(),
                studentName + " has returned from exeat", schoolId, "EXEAT_RETURNED");

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

        // State machine validation
        ExeatStatus current = exeat.getStatus();
        boolean validTransition = switch (current) {
            case PENDING -> newStatus == ExeatStatus.APPROVED || newStatus == ExeatStatus.DENIED;
            case APPROVED -> newStatus == ExeatStatus.RETURNED || newStatus == ExeatStatus.OVERDUE;
            default -> false;
        };

        if (!validTransition) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot transition from " + current + " to " + newStatus);
        }

        exeat.setStatus(newStatus);
        Exeat saved = exeatRepository.save(exeat);

        String studentName = resolveStudentName(exeat.getStudentId());
        notifyParents(exeat.getStudentId(),
                studentName + " exeat status updated to " + newStatus, schoolId, "EXEAT_STATUS");

        return toResponse(saved);
    }

    private String resolveStudentName(Long studentId) {
        Student student = studentRepository.findById(studentId).orElse(null);
        return student != null ? student.getFirstName() + " " + student.getLastName() : "Student #" + studentId;
    }

    public String getExeatsReportCsv(Long schoolId) {
        StringBuilder csv = new StringBuilder();
        csv.append("Exeat ID,Student ID,Student Name,Class,Reason,Status,Created At,Expected Return,Actual Return,Approved By\n");

        for (ExeatResponse e : getExeatsBySchoolResolved(schoolId)) {
            csv.append(e.getId())
                    .append(',').append(e.getStudentId())
                    .append(',').append(escapeCsv(e.getStudentName()))
                    .append(',').append(escapeCsv(e.getStudentClass()))
                    .append(',').append(escapeCsv(e.getReason()))
                    .append(',').append(e.getStatus())
                    .append(',').append(e.getCreatedAt() != null ? e.getCreatedAt() : "")
                    .append(',').append(e.getExpectedReturn() != null ? e.getExpectedReturn() : "")
                    .append(',').append(e.getActualReturn() != null ? e.getActualReturn() : "")
                    .append(',').append(escapeCsv(e.getApprovedByName()))
                    .append('\n');
        }
        return csv.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private void notifyParents(Long studentId, String message, Long schoolId, String type) {
        if (!settingsService.isExeatAlertsEnabled(schoolId)) return;

        List<StudentParent> links = studentParentRepository.findByStudentId(studentId);
        for (StudentParent link : links) {
            Long parentId = link.getId().getParentId();
            Optional<Parent> parentOpt = parentRepository.findById(parentId);

            try {
                boolean pushEnabled = preferenceService.isPushEnabled(
                        "PARENT", parentId, PreferenceService.EXEAT_CATEGORY);
                notificationService.notify(parentId, message, schoolId, type, pushEnabled);
            } catch (Exception e) {
                log.error("Failed to notify parent {} for student {}: {}", parentId, studentId, e.getMessage());
            }

            parentOpt.ifPresent(parent -> {
                if (parent.getEmail() != null && !parent.getEmail().isBlank()
                        && preferenceService.isEmailEnabled("PARENT", parentId, PreferenceService.EXEAT_CATEGORY)) {
                    try {
                        sendEmail(parent.getEmail(), message);
                    } catch (Exception e) {
                        log.error("Failed to email parent {} for student {}: {}", parentId, studentId, e.getMessage());
                    }
                }

                if (parent.getPhone() != null && !parent.getPhone().isBlank()
                        && preferenceService.isSmsEnabled("PARENT", parentId, PreferenceService.EXEAT_CATEGORY)) {
                    try {
                        smsService.sendSms(parent.getPhone(), message + " School: " + schoolName);
                    } catch (Exception e) {
                        log.error("Failed to SMS parent {} for student {}: {}", parentId, studentId, e.getMessage());
                    }
                }
            });
        }
    }

    private void sendEmail(String toEmail, String message) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(toEmail);
        mail.setSubject("BuzzApp — Exeat Update");
        mail.setText(message + "\n\nSchool: " + schoolName + "\n\n— BuzzApp Team");
        mail.setFrom("noreply@buzzapp.com");
        mailSender.send(mail);
        log.info("Exeat email sent to {}", toEmail);
    }

    private Exeat getOwnedExeat(Long exeatId, Long schoolId) {
        Exeat exeat = exeatRepository.findById(exeatId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exeat not found"));

        if (!exeat.getSchoolId().equals(schoolId)) {
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
        response.setNotes(e.getNotes());
        response.setApprovedBy(e.getApprovedBy());
        response.setExpectedReturn(e.getExpectedReturn());
        response.setActualReturn(e.getActualReturn());
        response.setStatus(e.getStatus());
        response.setCreatedAt(e.getCreatedAt());

        // Batch-resolve student names
        Student student = studentRepository.findById(e.getStudentId()).orElse(null);
        if (student != null) {
            response.setStudentName(student.getFirstName() + " " + student.getLastName());
            response.setStudentClass(student.getClassName());
        } else {
            response.setStudentName("Student #" + e.getStudentId());
            response.setStudentClass("—");
        }

        // Batch-resolve approver name
        if (e.getApprovedBy() != null) {
            User approver = userRepository.findById(e.getApprovedBy()).orElse(null);
            response.setApprovedByName(approver != null ? approver.getUsername() : null);
        }

        return response;
    }

    // Batch-optimized version for list responses
    public List<ExeatResponse> getExeatsBySchoolResolved(Long schoolId) {
        List<Exeat> exeats = exeatRepository.findBySchoolIdOrderByCreatedAtDesc(schoolId);
        return resolveAll(exeats);
    }

    public List<ExeatResponse> getExeatsByStudentResolved(Long studentId, Long schoolId) {
        List<Exeat> exeats = exeatRepository.findByStudentIdAndSchoolIdOrderByCreatedAtDesc(studentId, schoolId);
        return resolveAll(exeats);
    }

    private List<ExeatResponse> resolveAll(List<Exeat> exeats) {
        if (exeats.isEmpty()) return List.of();

        Set<Long> studentIds = exeats.stream().map(Exeat::getStudentId).collect(Collectors.toSet());
        Set<Long> approverIds = exeats.stream().filter(e -> e.getApprovedBy() != null)
                .map(Exeat::getApprovedBy).collect(Collectors.toSet());

        Map<Long, Student> studentMap = studentRepository.findByIdIn(List.copyOf(studentIds)).stream()
                .collect(Collectors.toMap(Student::getId, s -> s));
        Map<Long, User> approverMap = approverIds.isEmpty() ? Map.of() :
                userRepository.findByIdIn(List.copyOf(approverIds)).stream()
                        .collect(Collectors.toMap(User::getId, u -> u));

        return exeats.stream().map(e -> {
            ExeatResponse response = new ExeatResponse();
            response.setId(e.getId());
            response.setStudentId(e.getStudentId());
            response.setSchoolId(e.getSchoolId());
            response.setReason(e.getReason());
            response.setNotes(e.getNotes());
            response.setApprovedBy(e.getApprovedBy());
            response.setExpectedReturn(e.getExpectedReturn());
            response.setActualReturn(e.getActualReturn());
            response.setStatus(e.getStatus());
            response.setCreatedAt(e.getCreatedAt());

            Student student = studentMap.get(e.getStudentId());
            response.setStudentName(student != null ? student.getFirstName() + " " + student.getLastName() : "Student #" + e.getStudentId());
            response.setStudentClass(student != null ? student.getClassName() : "—");

            if (e.getApprovedBy() != null) {
                User approver = approverMap.get(e.getApprovedBy());
                response.setApprovedByName(approver != null ? approver.getUsername() : null);
            }

            return response;
        }).toList();
    }
}
