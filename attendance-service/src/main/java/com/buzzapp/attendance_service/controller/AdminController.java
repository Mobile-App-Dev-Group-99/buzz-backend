package com.buzzapp.attendance_service.controller;

import com.buzzapp.attendance_service.dto.*;
import com.buzzapp.attendance_service.model.*;
import com.buzzapp.attendance_service.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;
    private final StudentParentRepository studentParentRepository;

    @PostMapping("/student")
    public ResponseEntity<StudentResponse> createStudent(
            @RequestBody CreateStudentRequest request,
            Authentication auth) {
        Long schoolId = (Long) auth.getPrincipal();

        Student student = new Student();
        student.setFirstName(request.getFirstName());
        student.setLastName(request.getLastName());
        student.setClassName(request.getClassName());
        student.setSchoolId(schoolId);
        if (request.getGender() != null) {
            student.setGender(Gender.valueOf(request.getGender()));
        }
        if (request.getStudentType() != null) {
            student.setStudentType(StudentType.valueOf(request.getStudentType()));
        }

        Student saved = studentRepository.save(student);
        return ResponseEntity.ok(toStudentResponse(saved));
    }

    @PostMapping("/parent")
    public ResponseEntity<ParentResponse> createParent(
            @RequestBody CreateParentRequest request,
            Authentication auth) {
        Parent parent = new Parent();
        parent.setFirstName(request.getFirstName());
        parent.setLastName(request.getLastName());
        parent.setPhone(request.getPhone());

        Parent saved = parentRepository.save(parent);
        return ResponseEntity.ok(toParentResponse(saved));
    }

    @PostMapping("/link")
    public ResponseEntity<?> linkStudentParent(
            @RequestBody LinkRequest request,
            Authentication auth) {
        if (!studentRepository.existsById(request.getStudentId())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Student not found"));
        }
        if (!parentRepository.existsById(request.getParentId())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Parent not found"));
        }

        StudentParentId id = new StudentParentId(request.getStudentId(), request.getParentId());
        if (studentParentRepository.existsById(id)) {
            return ResponseEntity.ok(Map.of("message", "Already linked"));
        }

        studentParentRepository.save(new StudentParent(id));
        return ResponseEntity.ok(Map.of("message", "Linked successfully"));
    }

    @GetMapping("/students")
    public ResponseEntity<List<StudentResponse>> listStudents(Authentication auth) {
        Long schoolId = (Long) auth.getPrincipal();
        List<StudentResponse> students = studentRepository.findBySchoolId(schoolId)
                .stream()
                .map(this::toStudentResponse)
                .toList();
        return ResponseEntity.ok(students);
    }

    @GetMapping("/parents")
    public ResponseEntity<List<ParentResponse>> listParents(Authentication auth) {
        List<ParentResponse> parents = parentRepository.findAll()
                .stream()
                .map(this::toParentResponse)
                .toList();
        return ResponseEntity.ok(parents);
    }

    private StudentResponse toStudentResponse(Student s) {
        StudentResponse r = new StudentResponse();
        r.setId(s.getId());
        r.setFirstName(s.getFirstName());
        r.setLastName(s.getLastName());
        r.setClassName(s.getClassName());
        r.setGender(s.getGender() != null ? s.getGender().name() : null);
        r.setStudentType(s.getStudentType() != null ? s.getStudentType().name() : null);
        r.setSchoolId(s.getSchoolId());
        return r;
    }

    private ParentResponse toParentResponse(Parent p) {
        ParentResponse r = new ParentResponse();
        r.setId(p.getId());
        r.setFirstName(p.getFirstName());
        r.setLastName(p.getLastName());
        r.setPhone(p.getPhone());
        return r;
    }
}
