package com.buzzapp.attendance_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.StringJoiner;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService {

    private static final String AFRICASTALKING_URL = "https://api.africastalking.com/version1/messaging";

    @Value("${sms.username:}")
    private String smsUsername;

    @Value("${sms.apiKey:}")
    private String smsApiKey;

    @Value("${sms.senderId:}")
    private String smsSenderId;

    public boolean isConfigured() {
        return smsUsername != null && !smsUsername.isBlank()
                && smsApiKey != null && !smsApiKey.isBlank();
    }

    @Async
    public void sendSms(String to, String message) {
        if (!isConfigured()) {
            log.info("SMS not configured — skipping message to {}", to);
            return;
        }

        try {
            StringJoiner form = new StringJoiner("&");
            form.add("username=" + url(smsUsername));
            form.add("to=" + url(to));
            form.add("message=" + url(message));
            if (smsSenderId != null && !smsSenderId.isBlank()) {
                form.add("from=" + url(smsSenderId));
            }

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(AFRICASTALKING_URL))
                    .timeout(Duration.ofSeconds(15))
                    .header("apiKey", smsApiKey)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(form.toString()))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                log.warn("SMS send returned {}: {}", response.statusCode(), response.body());
            } else {
                log.info("SMS sent to {}", to);
            }
        } catch (Exception e) {
            log.warn("Failed to send SMS: {}", e.getMessage());
        }
    }

    private String url(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
