package com.buzzapp.attendance_service.service;

import com.buzzapp.attendance_service.dto.SchoolSettingsResponse;
import com.buzzapp.attendance_service.model.*;
import com.buzzapp.attendance_service.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceNotificationService {

    private final NotificationRepository notificationRepository;
    private final StudentParentRepository studentParentRepository;
    private final ParentRepository parentRepository;
    private final JavaMailSender mailSender;
    private final PushNotificationService pushNotificationService;
    private final SettingsService settingsService;
    private final PreferenceService preferenceService;
    private final SmsService smsService;

    @Value("${app.school.name:BuzzApp}")
    private String schoolName;

    @Async
    public void notifyParents(Student student, AttendanceStatus status, Long schoolId) {
        String category = switch (status) {
            case LATE -> "LATE";
            case ABSENT -> "ABSENT";
            default -> "ATTENDANCE";
        };

        SchoolSettingsResponse settings = settingsService.getSettings(schoolId);
        boolean schoolEnabled = switch (category) {
            case "LATE" -> settings.isAlertsLate();
            case "ABSENT" -> settings.isAlertsAbsent();
            default -> true;
        };
        if (!schoolEnabled) return;

        List<StudentParent> links = studentParentRepository.findByStudentId(student.getId());
        if (links.isEmpty()) return;

        String studentName = student.getFirstName() + " " + student.getLastName();
        String timeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"));
        String message = buildMessage(studentName, status, timeStr);

        for (StudentParent link : links) {
            Long parentId = link.getId().getParentId();

            // 1. Persist in-app notification
            try {
                Notification notification = new Notification();
                notification.setParentId(parentId);
                notification.setRecipientId(parentId);
                notification.setRecipientRole("PARENT");
                notification.setSchoolId(schoolId);
                notification.setMessage(message);
                notification.setType(status.name());
                notification.setRead(false);
                notification.setSentAt(LocalDateTime.now());
                notificationRepository.save(notification);
            } catch (Exception e) {
                log.error("Failed to create notification for parent {}: {}", parentId, e.getMessage());
            }

            // 2. Push — only if the parent has push enabled for this category
            if (preferenceService.isPushEnabled("PARENT", parentId, category)) {
                try {
                    pushNotificationService.pushToRecipient("PARENT", parentId, schoolId, "BuzzApp", message);
                } catch (Exception e) {
                    log.error("Failed to push to parent {}: {}", parentId, e.getMessage());
                }
            }

            // 3. Email — only if enabled for this category and an address exists
            if (preferenceService.isEmailEnabled("PARENT", parentId, category)) {
                try {
                    Parent parent = parentRepository.findById(parentId).orElse(null);
                    if (parent != null && parent.getEmail() != null && !parent.getEmail().isBlank()) {
                        sendEmail(parent.getEmail(), studentName, status, timeStr);
                    }
                } catch (Exception e) {
                    log.error("Failed to send email to parent {}: {}", parentId, e.getMessage());
                }
            }

            // 4. SMS — only if the parent opted in and has a phone number
            if (preferenceService.isSmsEnabled("PARENT", parentId, category)) {
                try {
                    Parent parent = parentRepository.findById(parentId).orElse(null);
                    if (parent != null && parent.getPhone() != null && !parent.getPhone().isBlank()) {
                        smsService.sendSms(parent.getPhone(), message + " School: " + schoolName);
                    }
                } catch (Exception e) {
                    log.error("Failed to send SMS to parent {}: {}", parentId, e.getMessage());
                }
            }
        }
    }

    private String buildMessage(String studentName, AttendanceStatus status, String time) {
        return switch (status) {
            case ARRIVED -> studentName + " has arrived at school at " + time + ".";
            case LATE -> studentName + " arrived late at school at " + time + ".";
            case DEPARTED -> studentName + " has left school at " + time + ".";
            default -> studentName + " attendance updated at " + time + ".";
        };
    }

    private void sendEmail(String toEmail, String studentName, AttendanceStatus status, String time) {
        String subject = "BuzzApp — Attendance Update for " + studentName;
        String body = buildEmailBody(studentName, status, time);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);
        message.setFrom("noreply@buzzapp.com");
        mailSender.send(message);
        log.info("Attendance email sent to {} for student {}", toEmail, studentName);
    }

    private String buildEmailBody(String studentName, AttendanceStatus status, String time) {
        String statusLine = switch (status) {
            case ARRIVED -> "has arrived at school";
            case LATE -> "arrived late at school";
            case DEPARTED -> "has left school";
            default -> "attendance has been updated";
        };

        return "Hello,\n\n" +
                "This is to inform you that " + studentName + " " + statusLine + " at " + time + ".\n\n" +
                "School: " + schoolName + "\n\n" +
                "— BuzzApp Team";
    }
}
