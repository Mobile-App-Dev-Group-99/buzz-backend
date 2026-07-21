package com.buzzapp.attendance_service.controller;

import com.buzzapp.attendance_service.model.Parent;
import com.buzzapp.attendance_service.model.Student;
import com.buzzapp.attendance_service.model.StudentParent;
import com.buzzapp.attendance_service.model.User;
import com.buzzapp.attendance_service.repository.ParentRepository;
import com.buzzapp.attendance_service.repository.StudentParentRepository;
import com.buzzapp.attendance_service.repository.StudentRepository;
import com.buzzapp.attendance_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequiredArgsConstructor
public class IdentityController {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;
    private final StudentParentRepository studentParentRepository;

    @GetMapping("/api/student/me")
    public ResponseEntity<?> getMe(Authentication auth) {
        String role = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst().orElse("");
        if (!role.equals("ROLE_STUDENT")) {
            return ResponseEntity.status(403).body("Access denied");
        }

        String email = (String) auth.getCredentials();
        if (email == null) {
            return ResponseEntity.status(401).body("Could not resolve identity");
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }

        Student student = studentRepository.findByUserId(user.getId()).orElse(null);
        if (student == null) {
            return ResponseEntity.status(404).body("Student record not found");
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", student.getId());
        body.put("firstName", student.getFirstName());
        body.put("lastName", student.getLastName());
        body.put("className", student.getClassName());
        body.put("schoolId", student.getSchoolId());
        return ResponseEntity.ok(body);
    }

    @GetMapping("/api/parent/me/children")
    public ResponseEntity<?> getMyChildren(Authentication auth) {
        String role = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst().orElse("");
        if (!role.equals("ROLE_PARENT")) {
            return ResponseEntity.status(403).body("Access denied");
        }

        String email = (String) auth.getCredentials();
        if (email == null) {
            return ResponseEntity.status(401).body("Could not resolve identity");
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }

        Parent parent = parentRepository.findByUserId(user.getId()).orElse(null);
        if (parent == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        Map<String, Object> parentResponse = new LinkedHashMap<>();
        parentResponse.put("parentId", parent.getId());
        parentResponse.put("firstName", parent.getFirstName());
        parentResponse.put("lastName", parent.getLastName());

        List<StudentParent> links = studentParentRepository.findByIdParentId(parent.getId());
        List<Map<String, Object>> children = new ArrayList<>();
        for (StudentParent link : links) {
            Student student = studentRepository.findById(link.getId().getStudentId()).orElse(null);
            if (student != null) {
                Map<String, Object> child = new LinkedHashMap<>();
                child.put("id", student.getId());
                child.put("firstName", student.getFirstName());
                child.put("lastName", student.getLastName());
                child.put("className", student.getClassName());
                child.put("schoolId", student.getSchoolId());
                children.add(child);
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("parent", parentResponse);
        result.put("children", children);
        return ResponseEntity.ok(result);
    }
}
