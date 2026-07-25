package com.buzzapp.auth_service.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.school.name:BuzzApp}")
    private String schoolName;

    public void sendTempPassword(String toEmail, String tempPassword) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("BuzzApp — Your Temporary Password");
            message.setText(
                "Hello,\n\n" +
                "Your temporary password for " + schoolName + " has been reset.\n\n" +
                "Temporary Password: " + tempPassword + "\n\n" +
                "Please log in with this password and change it immediately from your profile settings.\n\n" +
                "If you did not request this, please contact your school administrator.\n\n" +
                "— BuzzApp Team"
            );
            message.setFrom("noreply@buzzapp.com");
            mailSender.send(message);
            log.info("Temp password email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send temp password email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send email. Please contact your administrator.");
        }
    }
}
