package com.buzzapp.attendance_service.controller;

import com.buzzapp.attendance_service.dto.*;
import com.buzzapp.attendance_service.model.*;
import com.buzzapp.attendance_service.repository.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;
    private final StudentParentRepository studentParentRepository;
    private final TeacherClassRepository teacherClassRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/student")
    public ResponseEntity<?> createStudent(
            @Valid @RequestBody CreateStudentRequest request,
            Authentication auth) {
        Long schoolId = (Long) auth.getPrincipal();

        String email = request.getEmail();
        String password = request.getPassword();
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email and password are required"));
        }
        if (userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email already registered"));
        }

        User user = new User();
        user.setUsername(request.getFirstName() + " " + request.getLastName());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("STUDENT");
        user.setSchoolId(schoolId);
        User savedUser = userRepository.save(user);

        Student student = new Student();
        student.setFirstName(request.getFirstName());
        student.setLastName(request.getLastName());
        student.setClassName(request.getClassName());
        student.setSchoolId(schoolId);
        student.setUserId(savedUser.getId());
        if (request.getGender() != null) {
            student.setGender(Gender.valueOf(request.getGender()));
        }
        if (request.getStudentType() != null) {
            student.setStudentType(StudentType.valueOf(request.getStudentType()));
        }

        Student saved = studentRepository.save(student);
        StudentResponse resp = toStudentResponse(saved);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/parent")
    public ResponseEntity<?> createParent(
            @Valid @RequestBody CreateParentRequest request,
            Authentication auth) {
        String email = request.getEmail();
        String password = request.getPassword();
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email and password are required"));
        }
        if (userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email already registered"));
        }

        Long schoolId = (Long) auth.getPrincipal();

        User user = new User();
        user.setUsername(request.getFirstName() + " " + request.getLastName());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("PARENT");
        user.setSchoolId(schoolId);
        User savedUser = userRepository.save(user);

        Parent parent = new Parent();
        parent.setFirstName(request.getFirstName());
        parent.setLastName(request.getLastName());
        parent.setPhone(request.getPhone());
        parent.setEmail(email);
        parent.setUserId(savedUser.getId());

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
        Long schoolId = (Long) auth.getPrincipal();
        List<User> schoolUsers = userRepository.findBySchoolId(schoolId);
        java.util.Set<Long> userIds = schoolUsers.stream().map(User::getId).collect(java.util.stream.Collectors.toSet());
        List<ParentResponse> parents = parentRepository.findAll()
                .stream()
                .filter(p -> p.getUserId() != null && userIds.contains(p.getUserId()))
                .map(this::toParentResponse)
                .toList();
        return ResponseEntity.ok(parents);
    }

    @GetMapping("/teachers")
    public ResponseEntity<List<Map<String, Object>>> listTeachers(Authentication auth) {
        Long schoolId = (Long) auth.getPrincipal();
        List<User> teachers = userRepository.findByRoleAndSchoolId("TEACHER", schoolId);

        List<TeacherClass> assignments = teacherClassRepository.findBySchoolId(schoolId);
        Map<Long, String> classByTeacher = new HashMap<>();
        for (TeacherClass tc : assignments) {
            classByTeacher.put(tc.getTeacherUserId(), tc.getClassName());
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (User t : teachers) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", t.getId());
            item.put("username", t.getUsername());
            item.put("email", t.getEmail());
            item.put("className", classByTeacher.getOrDefault(t.getId(), null));
            result.add(item);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/teacher-class")
    public ResponseEntity<?> assignTeacherClass(
            @RequestBody AssignTeacherClassRequest request,
            Authentication auth) {
        Long schoolId = (Long) auth.getPrincipal();

        User teacher = userRepository.findById(request.getTeacherUserId()).orElse(null);
        if (teacher == null || !"TEACHER".equals(teacher.getRole())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Teacher not found"));
        }
        if (!schoolId.equals(teacher.getSchoolId())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Teacher not in this school"));
        }

        teacherClassRepository.deleteByTeacherUserIdAndSchoolId(request.getTeacherUserId(), schoolId);

        TeacherClass tc = new TeacherClass();
        tc.setTeacherUserId(request.getTeacherUserId());
        tc.setClassName(request.getClassName());
        tc.setSchoolId(schoolId);
        teacherClassRepository.save(tc);

        return ResponseEntity.ok(Map.of("message", "Teacher assigned to " + request.getClassName()));
    }

    @GetMapping("/teacher-class/{className}")
    public ResponseEntity<?> getTeacherForClass(
            @PathVariable String className,
            Authentication auth) {
        Long schoolId = (Long) auth.getPrincipal();
        return teacherClassRepository.findByClassNameAndSchoolId(className, schoolId)
                .map(tc -> {
                    User teacher = userRepository.findById(tc.getTeacherUserId()).orElse(null);
                    Map<String, Object> body = new LinkedHashMap<>();
                    body.put("teacherUserId", tc.getTeacherUserId());
                    body.put("teacherName", teacher != null ? teacher.getUsername() : "Unknown");
                    body.put("className", tc.getClassName());
                    return ResponseEntity.ok((Object) body);
                })
                .orElse(ResponseEntity.ok(Map.of("className", className, "teacherUserId", null, "teacherName", null)));
    }

    @GetMapping("/teacher-classes")
    public ResponseEntity<List<Map<String, Object>>> listTeacherClasses(Authentication auth) {
        Long schoolId = (Long) auth.getPrincipal();
        List<TeacherClass> classes = teacherClassRepository.findBySchoolId(schoolId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (TeacherClass tc : classes) {
            User teacher = userRepository.findById(tc.getTeacherUserId()).orElse(null);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", tc.getId());
            item.put("teacherUserId", tc.getTeacherUserId());
            item.put("teacherName", teacher != null ? teacher.getUsername() : "Unknown");
            item.put("className", tc.getClassName());
            result.add(item);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/students/class/{className}")
    public ResponseEntity<List<StudentResponse>> listStudentsByClass(
            @PathVariable String className,
            Authentication auth) {
        Long schoolId = (Long) auth.getPrincipal();
        List<StudentResponse> students = studentRepository.findBySchoolId(schoolId)
                .stream()
                .filter(s -> className.equals(s.getClassName()))
                .map(this::toStudentResponse)
                .toList();
        return ResponseEntity.ok(students);
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
