package com.buzzapp.attendance_service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

@Service
public class TokenValidationService {

    @Value("${auth.service.url}")
    private String authServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public boolean validate(String token) {
        throw new UnsupportedOperationException("Token validation not yet implemented");
    }

    public String extractRole(String token) {
        throw new UnsupportedOperationException("Role extraction not yet implemented");
    }

    public Long extractSchoolId(String token) {
        throw new UnsupportedOperationException("SchoolId extraction not yet implemented");
    }
}