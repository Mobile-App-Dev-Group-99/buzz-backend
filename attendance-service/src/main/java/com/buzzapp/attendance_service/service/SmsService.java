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
import java.util.Base64;
import java.util.StringJoiner;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService {

    private static final String TWILIO_URL = "https://api.twilio.com/2010-04-01/Accounts/";

    @Value("${twilio.accountSid:}")
    private String twilioAccountSid;

    @Value("${twilio.authToken:}")
    private String twilioAuthToken;

    @Value("${twilio.fromNumber:}")
    private String twilioFromNumber;

    public boolean isConfigured() {
        return twilioAccountSid != null && !twilioAccountSid.isBlank()
                && twilioAuthToken != null && !twilioAuthToken.isBlank()
                && twilioFromNumber != null && !twilioFromNumber.isBlank();
    }

    @Async
    public void sendSms(String to, String message) {
        if (!isConfigured()) {
            log.info("SMS not configured — skipping message to {}", to);
            return;
        }

        try {
            String recipient = normalizePhone(to);

            StringJoiner form = new StringJoiner("&");
            form.add("To=" + url(recipient));
            form.add("From=" + url(twilioFromNumber));
            form.add("Body=" + url(message));

            String credentials = Base64.getEncoder().encodeToString(
                    (twilioAccountSid + ":" + twilioAuthToken).getBytes(StandardCharsets.UTF_8));

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(TWILIO_URL + twilioAccountSid + "/Messages.json"))
                    .timeout(Duration.ofSeconds(15))
                    .header("Authorization", "Basic " + credentials)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(form.toString()))
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

    private String url(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
