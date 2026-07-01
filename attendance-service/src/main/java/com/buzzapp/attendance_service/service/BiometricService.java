package com.buzzapp.attendance_service.service;

import com.buzzapp.attendance_service.dto.BiometricRegisterRequest;
import com.buzzapp.attendance_service.dto.BiometricRegisterResponse;
import com.buzzapp.attendance_service.model.BiometricTemplate;
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
        studentRepository.findById(request.getStudentId())
                .filter(s -> s.getSchoolId().equals(schoolId))
                .orElseThrow(() -> new RuntimeException("Student not found in this school"));

        BiometricTemplate template = new BiometricTemplate();
        template.setStudentId(request.getStudentId());
        template.setTemplate(request.getTemplate());
        template.setCreatedAt(LocalDateTime.now());

        BiometricTemplate saved = biometricTemplateRepository.save(template);

        BiometricRegisterResponse response = new BiometricRegisterResponse();
        response.setId(saved.getId());
        response.setStudentId(saved.getStudentId());
        response.setCreatedAt(saved.getCreatedAt());
        return response;
    }
}