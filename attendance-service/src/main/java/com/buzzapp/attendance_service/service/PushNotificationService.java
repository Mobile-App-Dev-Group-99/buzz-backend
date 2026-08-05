package com.buzzapp.attendance_service.service;

import com.buzzapp.attendance_service.model.DeviceToken;
import com.buzzapp.attendance_service.repository.DeviceTokenRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PushNotificationService {

    private static final String EXPO_PUSH_URL = "https://exp.host/--/api/v2/push/send";

    private final DeviceTokenRepository deviceTokenRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Async
    public void pushToRecipient(String role, Long recipientId, Long schoolId, String title, String body) {
        List<DeviceToken> tokens = deviceTokenRepository
                .findByRoleAndRecipientIdAndSchoolId(role.toUpperCase(), recipientId, schoolId);
        if (tokens == null || tokens.isEmpty()) return;

        List<Map<String, Object>> messages = tokens.stream()
                .map(t -> Map.<String, Object>of(
                        "to", t.getToken(),
                        "title", title,
                        "body", body,
                        "sound", "default"))
                .toList();

        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(EXPO_PUSH_URL))
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(messages)))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                log.warn("Expo push returned {}: {}", response.statusCode(), response.body());
            }
        } catch (Exception e) {
            log.warn("Failed to send push notification: {}", e.getMessage());
        }
    }
}
