package com.buzzapp.safety_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService {

    private static final String HUBTEL_URL = "https://smsc.hubtel.com/v1/messages/send";

    @Value("${sms.clientId:}")
    private String smsClientId;

    @Value("${sms.clientSecret:}")
    private String smsClientSecret;

    @Value("${sms.senderId:}")
    private String smsSenderId;

    public boolean isConfigured() {
        return smsClientId != null && !smsClientId.isBlank()
                && smsClientSecret != null && !smsClientSecret.isBlank();
    }

    @Async
    public void sendSms(String to, String message) {
        if (!isConfigured()) {
            log.info("SMS not configured — skipping message to {}", to);
            return;
        }

        try {
            String recipient = normalizePhone(to);

            Map<String, String> body = new LinkedHashMap<>();
            if (smsSenderId != null && !smsSenderId.isBlank()) {
                body.put("from", smsSenderId);
            }
            body.put("to", recipient);
            body.put("content", message);

            String credentials = Base64.getEncoder().encodeToString(
                    (smsClientId + ":" + smsClientSecret).getBytes(StandardCharsets.UTF_8));

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(HUBTEL_URL))
                    .timeout(Duration.ofSeconds(15))
                    .header("Authorization", "Basic " + credentials)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(toJson(body)))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                log.warn("SMS send returned {}: {}", response.statusCode(), response.body());
            } else {
                log.info("SMS sent to {}", recipient);
            }
        } catch (Exception e) {
            log.warn("Failed to send SMS: {}", e.getMessage());
        }
    }

    private String normalizePhone(String phone) {
        if (phone == null) return null;
        String cleaned = phone.replaceAll("[^0-9+]", "");
        if (cleaned.startsWith("0")) {
            return "233" + cleaned.substring(1);
        }
        if (!cleaned.startsWith("+")) {
            return "+" + cleaned;
        }
        return cleaned;
    }

    private String toJson(Map<String, String> values) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            if (!first) json.append(",");
            first = false;
            json.append('"').append(entry.getKey()).append("\":\"")
                    .append(escapeJson(entry.getValue())).append('"');
        }
        return json.append('}').toString();
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
