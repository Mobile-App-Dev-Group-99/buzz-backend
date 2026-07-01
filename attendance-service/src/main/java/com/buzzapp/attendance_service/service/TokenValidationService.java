package com.buzzapp.attendance_service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class TokenValidationService {

    @Value("${auth.service.url}")
    private String authServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    private ResponseEntity<Map> callValidate(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(
                authServiceUrl + "/api/auth/validate",
                HttpMethod.GET,
                entity,
                Map.class
        );
    }

    public boolean validate(String token) {
        try {
            ResponseEntity<Map> response = callValidate(token);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            return false;
        }
    }

    public String extractRole(String token) {
        try {
            ResponseEntity<Map> response = callValidate(token);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return (String) response.getBody().get("role");
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    public Long extractSchoolId(String token) {
        try {
            ResponseEntity<Map> response = callValidate(token);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Object val = response.getBody().get("schoolId");
                if (val instanceof Integer) return ((Integer) val).longValue();
                if (val instanceof Long) return (Long) val;
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
}