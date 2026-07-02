package com.buzzapp.auth_service.services;

import com.buzzapp.auth_service.dto.*;
import com.buzzapp.auth_service.model.Role;
import com.buzzapp.auth_service.model.School;
import com.buzzapp.auth_service.model.User;
import com.buzzapp.auth_service.repository.SchoolRepository;
import com.buzzapp.auth_service.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SchoolRepository schoolRepository;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtService jwtService, SchoolRepository schoolRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.schoolRepository = schoolRepository;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Wrong password");
        }
        String token = jwtService.generateToken(user.getEmail(), user.getRole().toString(), user.getSchool_id());
        return new LoginResponse(token, user.getRole().toString(), user.getEmail());
    }

    public String register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.valueOf(request.getRole()));
        userRepository.save(user);
        return "User registered successfully";
    }

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
}