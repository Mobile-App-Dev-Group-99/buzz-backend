package com.buzzapp.safety_service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class TokenValidationService {

    @Value("${auth.service.url}")
    private String authServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public TokenValidationResult validateAndExtract(String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    authServiceUrl + "/api/auth/validate",
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return new TokenValidationResult(false, null, null);
            }

            Map body = response.getBody();
            String role = body.get("role") != null ? body.get("role").toString() : null;
            Long schoolId = body.get("schoolId") != null ? Long.valueOf(body.get("schoolId").toString()) : null;

            return new TokenValidationResult(true, role, schoolId);
        } catch (RestClientException e) {
            return new TokenValidationResult(false, null, null);
        }
    }

    // Kept so nothing else that references these individually breaks.
    public boolean validate(String token) {
        return validateAndExtract(token).valid();
    }

    public String extractRole(String token) {
        return validateAndExtract(token).role();
    }

    public Long extractSchoolId(String token) {
        return validateAndExtract(token).schoolId();
    }
}