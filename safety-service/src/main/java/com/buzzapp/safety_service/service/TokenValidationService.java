package com.buzzapp.safety_service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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