# Email Service — BuzzApp

## Overview

The email service handles sending temporary passwords when users forget their passwords. It uses Gmail SMTP to send emails from a configured sender account.

## How It Works

```
User taps "Forgot Password"
  → Enters email address
  → POST /api/auth/forgot-password { email }
  → Backend generates temp password: "BuzzApp" + random 4 digits
  → Saves temp password to database (replaces old one)
  → EmailService sends temp password to user's email
  → User logs in with temp password
  → User changes password from Profile screen
```

## Flow Diagram

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│   Mobile /   │     │  auth-service │     │  Gmail SMTP  │     │    User's    │
│   Web App    │     │   (Backend)   │     │   (Sender)   │     │    Email     │
└──────┬───────┘     └──────┬───────┘     └──────┬───────┘     └──────┬───────┘
       │                    │                    │                    │
       │  POST /forgot-    │                    │                    │
       │  password {email} │                    │                    │
       │──────────────────>│                    │                    │
       │                    │                    │                    │
       │                    │  Generate temp     │                    │
       │                    │  password           │                    │
       │                    │  Save to DB         │                    │
       │                    │                    │                    │
       │                    │  Send email with    │                    │
       │                    │  temp password      │                    │
       │                    │───────────────────>│                    │
       │                    │                    │                    │
       │                    │                    │  Deliver email     │
       │                    │                    │───────────────────>│
       │                    │                    │                    │
       │  { message:       │                    │                    │
       │  "Password sent" }│                    │                    │
       │<──────────────────│                    │                    │
```

## Files Changed

| File | Change |
|------|--------|
| `auth-service/pom.xml` | Added `spring-boot-starter-mail` dependency |
| `auth-service/services/EmailService.java` | New — sends temp password emails via SMTP |
| `auth-service/services/AuthService.java` | Updated `forgotPassword()` to call EmailService |
| `auth-service/application.properties` | Added mail config (host, port, auth, TLS) |
| `render.yaml` | Added MAIL_HOST, MAIL_PORT, MAIL_USERNAME, MAIL_PASSWORD env vars |

## EmailService.java

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.school.name:BuzzApp}")
    private String schoolName;

    public void sendTempPassword(String toEmail, String tempPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("BuzzApp — Your Temporary Password");
        message.setText(
            "Hello,\n\n" +
            "Your temporary password for " + schoolName + " has been reset.\n\n" +
            "Temporary Password: " + tempPassword + "\n\n" +
            "Please log in with this password and change it immediately.\n\n" +
            "If you did not request this, contact your school administrator.\n\n" +
            "— BuzzApp Team"
        );
        message.setFrom("noreply@buzzapp.com");
        mailSender.send(message);
    }
}
```

## Email Message Received by User

```
Subject: BuzzApp — Your Temporary Password

Hello,

Your temporary password for BuzzApp has been reset.

Temporary Password: BuzzApp4827

Please log in with this password and change it immediately from your profile settings.

If you did not request this, please contact your school administrator.

— BuzzApp Team
```

## Environment Variables (Render)

| Variable | Value | Description |
|----------|-------|-------------|
| `MAIL_HOST` | `smtp.gmail.com` | Gmail SMTP server |
| `MAIL_PORT` | `587` | TLS port |
| `MAIL_USERNAME` | your-gmail@gmail.com | Sender email address |
| `MAIL_PASSWORD` | xxxx xxxx xxxx xxxx | Gmail app password (not your real password) |

## Setup Instructions

1. Create or use a Gmail account for the app
2. Enable 2-Factor Authentication on that Gmail
3. Go to https://myaccount.google.com/apppasswords
4. Generate an app password for "Mail"
5. On Render → auth-service → Environment tab:
   - Set `MAIL_USERNAME` = your Gmail address
   - Set `MAIL_PASSWORD` = the 16-character app password
6. Redeploy auth-service

## Security Notes

- Temp passwords are generated as `BuzzApp` + 4 random digits (e.g., `BuzzApp4827`)
- Temp passwords are bcrypt-hashed before saving to database
- Temp passwords do not expire — they remain valid until the user changes them
- The sender email (`noreply@buzzapp.com`) is used in the `From` field
- Users should change their password immediately after logging in with the temp password
