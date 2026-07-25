package com.buzzapp.auth_service.services;

import com.buzzapp.auth_service.dto.*;
import com.buzzapp.auth_service.model.*;
import com.buzzapp.auth_service.repository.SchoolRepository;
import com.buzzapp.auth_service.repository.StudentRepository;
import com.buzzapp.auth_service.repository.ParentRepository;
import com.buzzapp.auth_service.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SchoolRepository schoolRepository;
    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtService jwtService, SchoolRepository schoolRepository,
                       StudentRepository studentRepository, ParentRepository parentRepository,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.schoolRepository = schoolRepository;
        this.studentRepository = studentRepository;
        this.parentRepository = parentRepository;
        this.emailService = emailService;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("No account found with this email"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Incorrect password");
        }
        String token = jwtService.generateToken(user.getEmail(), user.getRole().toString(), user.getSchool_id());
        return new LoginResponse(user.getEmail(), token, user.getRole().toString(), user.getSchool_id());
    }

    @Transactional
    public String register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.valueOf(request.getRole()));
        user.setSchool_id(request.getSchoolId());
        User savedUser = userRepository.save(user);

        if (savedUser.getRole() == Role.STUDENT) {
            Student student = new Student();
            student.setSchoolId(request.getSchoolId());
            student.setUserId(savedUser.getId());
            student.setFirstName(request.getFirstName() != null ? request.getFirstName() : request.getUsername());
            student.setLastName(request.getLastName() != null ? request.getLastName() : "");
            student.setClassName(request.getClassName());
            student.setGender(request.getGender());
            student.setStudentType(request.getStudentType());
            studentRepository.save(student);
        } else if (savedUser.getRole() == Role.PARENT) {
            Parent parent = new Parent();
            parent.setUserId(savedUser.getId());
            parent.setFirstName(request.getFirstName() != null ? request.getFirstName() : request.getUsername());
            parent.setLastName(request.getLastName() != null ? request.getLastName() : "");
            parent.setPhone(request.getPhone());
            parent.setEmail(request.getEmail());
            parentRepository.save(parent);
        }

        return "User registered successfully";
    }

    @Transactional
    public OnboardSchoolResponse onboardSchool(OnboardSchoolRequest request) {
        School school = new School();
        school.setName(request.getSchoolName());
        school.setLocation(request.getLocation());
        school.setLevel(request.getLevel());
        school = schoolRepository.save(school);

        User admin = new User();
        admin.setUsername(request.getAdminUsername());
        admin.setEmail(request.getAdminEmail());
        admin.setPassword(passwordEncoder.encode(request.getAdminPassword()));
        admin.setRole(Role.ADMIN);
        admin.setSchool_id(school.getId());
        userRepository.save(admin);

        String token = jwtService.generateToken(admin.getEmail(), admin.getRole().name(), school.getId());

        return new OnboardSchoolResponse(token, admin.getRole().name(), admin.getEmail(), school.getId());
    }

    @Transactional
    public String forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No account found with this email"));

        String tempPassword = "BuzzApp" + (int)(Math.random() * 9000 + 1000);
        user.setPassword(passwordEncoder.encode(tempPassword));
        userRepository.save(user);

        emailService.sendTempPassword(email, tempPassword);

        return "A temporary password has been sent to your email";
    }

    @Transactional
    public void adminResetPassword(String email, String newPassword, org.springframework.security.core.Authentication auth) {
        String adminEmail = (String) auth.getPrincipal();
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (admin.getRole() != Role.ADMIN) {
            throw new RuntimeException("Only administrators can reset passwords");
        }

        User target = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        if (!admin.getSchool_id().equals(target.getSchool_id())) {
            throw new RuntimeException("Cannot reset password for users outside your school");
        }

        target.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(target);
    }
}