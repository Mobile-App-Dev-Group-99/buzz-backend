package com.buzzapp.attendance_service.controller;

import com.buzzapp.attendance_service.model.TeacherClass;
import com.buzzapp.attendance_service.model.User;
import com.buzzapp.attendance_service.repository.TeacherClassRepository;
import com.buzzapp.attendance_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherClassRepository teacherClassRepository;
    private final UserRepository userRepository;

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
}
