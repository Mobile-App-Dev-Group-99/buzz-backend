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

    public TokenClaims validateAndExtract(String token) {
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
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map body = response.getBody();
                String role = (String) body.get("role");
                Object schoolIdVal = body.get("schoolId");
                Long schoolId = null;
                if (schoolIdVal instanceof Integer) schoolId = ((Integer) schoolIdVal).longValue();
                else if (schoolIdVal instanceof Long) schoolId = (Long) schoolIdVal;
                String email = (String) body.get("email");
                return new TokenClaims(true, role, schoolId, email);
            }
        } catch (Exception e) {
        }
        return new TokenClaims(false, null, null, null);
    }

    public record TokenClaims(boolean valid, String role, Long schoolId, String email) {}
}