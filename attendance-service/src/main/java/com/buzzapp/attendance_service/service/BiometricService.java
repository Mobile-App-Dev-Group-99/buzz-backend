package com.buzzapp.attendance_service.service;

import com.buzzapp.attendance_service.dto.BiometricRegisterRequest;
import com.buzzapp.attendance_service.dto.BiometricRegisterResponse;
import com.buzzapp.attendance_service.dto.BiometricVerifyRequest;
import com.buzzapp.attendance_service.dto.BiometricVerifyResponse;
import com.buzzapp.attendance_service.model.BiometricTemplate;
import com.buzzapp.attendance_service.model.Student;
import com.buzzapp.attendance_service.repository.BiometricTemplateRepository;
import com.buzzapp.attendance_service.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BiometricService {

    private final BiometricTemplateRepository biometricTemplateRepository;
    private final StudentRepository studentRepository;

    public BiometricRegisterResponse registerTemplate(BiometricRegisterRequest request, Long schoolId) {
        Student student = studentRepository.findById(request.getStudentId())
                .filter(s -> s.getSchoolId().equals(schoolId))
                .orElseThrow(() -> new RuntimeException("Student not found in this school"));

        // Delete existing template for this student if re-enrolling
        biometricTemplateRepository.findByStudentId(request.getStudentId())
                .ifPresent(biometricTemplateRepository::delete);

        BiometricTemplate template = new BiometricTemplate();
        template.setStudentId(request.getStudentId());
        template.setTemplate(request.getTemplate());
        template.setSlotId(request.getSlotId());
        template.setCreatedAt(LocalDateTime.now());

        BiometricTemplate saved = biometricTemplateRepository.save(template);

        BiometricRegisterResponse response = new BiometricRegisterResponse();
        response.setId(saved.getId());
        response.setStudentId(saved.getStudentId());
        response.setSlotId(saved.getSlotId());
        response.setCreatedAt(saved.getCreatedAt());
        return response;
    }

    public BiometricVerifyResponse verifyTemplate(BiometricVerifyRequest request, Long schoolId) {
        BiometricTemplate template = biometricTemplateRepository.findBySlotId(request.getSlotId())
                .orElseThrow(() -> new RuntimeException("No biometric template found for this scanner slot"));

        Student student = studentRepository.findById(template.getStudentId())
                .filter(s -> s.getSchoolId().equals(schoolId))
                .orElseThrow(() -> new RuntimeException("Student not found in this school"));

        BiometricVerifyResponse response = new BiometricVerifyResponse();
        response.setStudentId(student.getId());
        response.setStudentName(student.getFirstName() + " " + student.getLastName());
        response.setStudentCode("BZ-" + student.getId());
        response.setClassName(student.getClassName());
        response.setTemplateId(template.getId());
        return response;
    }
}
