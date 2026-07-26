package com.buzzapp.attendance_service.controller;

import com.buzzapp.attendance_service.dto.TeacherCreateExeatRequest;
import com.buzzapp.attendance_service.dto.TeacherExeatResponse;
import com.buzzapp.attendance_service.model.*;
import com.buzzapp.attendance_service.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherClassRepository teacherClassRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final ExeatRepository exeatRepository;

    @GetMapping("/api/teacher/me/class")
    public ResponseEntity<?> getMyClass(Authentication auth) {
        String role = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst().orElse("");
        if (!role.equals("ROLE_TEACHER")) {
            return ResponseEntity.status(403).body("Access denied");
        }

        Long schoolId = (Long) auth.getPrincipal();
        String email = (String) auth.getCredentials();

        User teacher = userRepository.findByEmail(email).orElse(null);
        if (teacher == null) {
            return ResponseEntity.status(404).body("Teacher not found");
        }

        Optional<TeacherClass> tc = teacherClassRepository.findByTeacherUserIdAndSchoolId(teacher.getId(), schoolId);
        if (tc.isEmpty()) {
            return ResponseEntity.ok(Map.of("assigned", false, "className", null));
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("assigned", true);
        body.put("className", tc.get().getClassName());
        body.put("teacherId", teacher.getId());
        body.put("teacherName", teacher.getUsername());
        return ResponseEntity.ok(body);
    }

    @GetMapping("/api/teacher/exeats")
    public ResponseEntity<?> getMyClassExeats(Authentication auth) {
        Long schoolId = (Long) auth.getPrincipal();
        String email = (String) auth.getCredentials();

        User teacher = userRepository.findByEmail(email).orElse(null);
        if (teacher == null) {
            return ResponseEntity.status(404).body(Map.of("message", "Teacher not found"));
        }

        Optional<TeacherClass> tc = teacherClassRepository.findByTeacherUserIdAndSchoolId(teacher.getId(), schoolId);
        if (tc.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        List<Student> classStudents = studentRepository.findByClassNameAndSchoolId(tc.get().getClassName(), schoolId);
        List<Long> studentIds = classStudents.stream().map(Student::getId).collect(Collectors.toList());

        if (studentIds.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        List<Exeat> exeats = exeatRepository.findByStudentIdInAndSchoolIdOrderByCreatedAtDesc(studentIds, schoolId);

        Map<Long, Student> studentMap = classStudents.stream()
                .collect(Collectors.toMap(Student::getId, s -> s));

        List<User> allUsers = userRepository.findAll();
        Map<Long, String> userNameMap = allUsers.stream()
                .collect(Collectors.toMap(User::getId, u -> u.getUsername()));

        List<TeacherExeatResponse> result = exeats.stream().map(ex -> {
            TeacherExeatResponse resp = new TeacherExeatResponse();
            resp.setId(ex.getId());
            resp.setStudentId(ex.getStudentId());
            Student s = studentMap.get(ex.getStudentId());
            resp.setStudentName(s != null ? s.getFirstName() + " " + s.getLastName() : "Unknown");
            resp.setStudentClass(s != null ? s.getClassName() : "");
            resp.setReason(ex.getReason());
            resp.setApprovedBy(ex.getApprovedBy());
            resp.setApprovedByName(ex.getApprovedBy() != null ? userNameMap.get(ex.getApprovedBy()) : null);
            resp.setExpectedReturn(ex.getExpectedReturn());
            resp.setActualReturn(ex.getActualReturn());
            resp.setStatus(ex.getStatus().name());
            resp.setCreatedAt(ex.getCreatedAt());
            return resp;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @PostMapping("/api/teacher/exeat/create")
    public ResponseEntity<?> createExeat(@RequestBody TeacherCreateExeatRequest request,
                                         Authentication auth) {
        Long schoolId = (Long) auth.getPrincipal();
        String email = (String) auth.getCredentials();

        User teacher = userRepository.findByEmail(email).orElse(null);
        if (teacher == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Teacher not found"));
        }

        Optional<TeacherClass> tc = teacherClassRepository.findByTeacherUserIdAndSchoolId(teacher.getId(), schoolId);
        if (tc.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "You are not assigned to any class"));
        }

        Student student = studentRepository.findById(request.getStudentId()).orElse(null);
        if (student == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Student not found"));
        }
        if (!student.getSchoolId().equals(schoolId)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Student not in your school"));
        }
        if (!tc.get().getClassName().equals(student.getClassName())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Student is not in your class"));
        }

        Exeat exeat = new Exeat();
        exeat.setStudentId(student.getId());
        exeat.setSchoolId(schoolId);
        exeat.setReason(request.getReason());
        exeat.setStatus(ExeatStatus.PENDING);
        exeat.setCreatedAt(LocalDateTime.now());
        if (request.getExpectedReturn() != null) {
            exeat.setExpectedReturn(LocalDateTime.parse(request.getExpectedReturn()));
        }

        Exeat saved = exeatRepository.save(exeat);

        TeacherExeatResponse resp = new TeacherExeatResponse();
        resp.setId(saved.getId());
        resp.setStudentId(student.getId());
        resp.setStudentName(student.getFirstName() + " " + student.getLastName());
        resp.setStudentClass(student.getClassName());
        resp.setReason(saved.getReason());
        resp.setStatus(saved.getStatus().name());
        resp.setCreatedAt(saved.getCreatedAt());
        resp.setExpectedReturn(saved.getExpectedReturn());

        return ResponseEntity.ok(resp);
    }

    @PutMapping("/api/teacher/exeat/{id}/approve")
    public ResponseEntity<?> approveExeat(@PathVariable Long id, Authentication auth) {
        Long schoolId = (Long) auth.getPrincipal();
        String email = (String) auth.getCredentials();

        User teacher = userRepository.findByEmail(email).orElse(null);
        if (teacher == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Teacher not found"));
        }

        Optional<TeacherClass> tc = teacherClassRepository.findByTeacherUserIdAndSchoolId(teacher.getId(), schoolId);
        if (tc.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "You are not assigned to any class"));
        }

        Exeat exeat = exeatRepository.findById(id).orElse(null);
        if (exeat == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Exeat not found"));
        }
        if (!exeat.getSchoolId().equals(schoolId)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Access denied"));
        }
        if (exeat.getStatus() != ExeatStatus.PENDING) {
            return ResponseEntity.badRequest().body(Map.of("message", "Exeat is not pending"));
        }

        Student student = studentRepository.findById(exeat.getStudentId()).orElse(null);
        if (student == null || !tc.get().getClassName().equals(student.getClassName())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Student is not in your class"));
        }

        exeat.setStatus(ExeatStatus.APPROVED);
        exeat.setApprovedBy(teacher.getId());
        exeatRepository.save(exeat);

        return ResponseEntity.ok(Map.of("message", "Exeat approved", "status", "APPROVED"));
    }

    @PutMapping("/api/teacher/exeat/{id}/deny")
    public ResponseEntity<?> denyExeat(@PathVariable Long id, Authentication auth) {
        Long schoolId = (Long) auth.getPrincipal();
        String email = (String) auth.getCredentials();

        User teacher = userRepository.findByEmail(email).orElse(null);
        if (teacher == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Teacher not found"));
        }

        Optional<TeacherClass> tc = teacherClassRepository.findByTeacherUserIdAndSchoolId(teacher.getId(), schoolId);
        if (tc.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "You are not assigned to any class"));
        }

        Exeat exeat = exeatRepository.findById(id).orElse(null);
        if (exeat == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Exeat not found"));
        }
        if (!exeat.getSchoolId().equals(schoolId)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Access denied"));
        }
        if (exeat.getStatus() != ExeatStatus.PENDING) {
            return ResponseEntity.badRequest().body(Map.of("message", "Exeat is not pending"));
        }

        Student student = studentRepository.findById(exeat.getStudentId()).orElse(null);
        if (student == null || !tc.get().getClassName().equals(student.getClassName())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Student is not in your class"));
        }

        exeat.setStatus(ExeatStatus.DENIED);
        exeat.setApprovedBy(teacher.getId());
        exeatRepository.save(exeat);

        return ResponseEntity.ok(Map.of("message", "Exeat denied", "status", "DENIED"));
    }

    @PutMapping("/api/teacher/exeat/{id}/return")
    public ResponseEntity<?> recordReturn(@PathVariable Long id, Authentication auth) {
        Long schoolId = (Long) auth.getPrincipal();
        String email = (String) auth.getCredentials();

        User teacher = userRepository.findByEmail(email).orElse(null);
        if (teacher == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Teacher not found"));
        }

        Optional<TeacherClass> tc = teacherClassRepository.findByTeacherUserIdAndSchoolId(teacher.getId(), schoolId);
        if (tc.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "You are not assigned to any class"));
        }

        Exeat exeat = exeatRepository.findById(id).orElse(null);
        if (exeat == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Exeat not found"));
        }
        if (!exeat.getSchoolId().equals(schoolId)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Access denied"));
        }
        if (exeat.getStatus() != ExeatStatus.APPROVED) {
            return ResponseEntity.badRequest().body(Map.of("message", "Exeat must be APPROVED before recording return"));
        }

        Student student = studentRepository.findById(exeat.getStudentId()).orElse(null);
        if (student == null || !tc.get().getClassName().equals(student.getClassName())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Student is not in your class"));
        }

        exeat.setStatus(ExeatStatus.RETURNED);
        exeat.setActualReturn(LocalDateTime.now());
        exeatRepository.save(exeat);

        return ResponseEntity.ok(Map.of("message", "Return recorded", "status", "RETURNED"));
    }
}
