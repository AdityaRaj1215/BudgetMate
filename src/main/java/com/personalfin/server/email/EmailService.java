package com.personalfin.server.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:no-reply@personalfinance.com}")
    private String fromEmail;

    @Value("${spring.mail.enabled:true}")
    private boolean emailEnabled;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtpEmail(String toEmail, String otp) {
        if (!emailEnabled) {
            logger.warn("Email sending is disabled. OTP for {}: {}", toEmail, otp);
            // In dev mode, log OTP for testing
            if ("dev".equalsIgnoreCase(activeProfile)) {
                logger.warn("DEV MODE: Email disabled - OTP for {} is {}", toEmail, otp);
            }
            return;
        }

        // Check if email credentials are configured
        if (fromEmail == null || fromEmail.isEmpty() || fromEmail.equals("no-reply@personalfinance.com")) {
            logger.error("❌ Email not properly configured! EMAIL_FROM is not set or is default value.");
            logger.warn("Email not properly configured. OTP for {}: {}", toEmail, otp);
            if ("dev".equalsIgnoreCase(activeProfile)) {
                logger.warn("DEV MODE: Email not configured - OTP for {} is {}", toEmail, otp);
            }
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Your Registration OTP - Personal Finance Tracker");
            message.setText(buildOtpEmailBody(otp));

            logger.debug("Attempting to send email to {} from {}", toEmail, fromEmail);
            mailSender.send(message);
            logger.info("✅ OTP email sent successfully to {}", toEmail);
        } catch (MailException e) {
            logger.error("❌ Failed to send OTP email to {}: {}", toEmail, e.getMessage());
            logger.error("Error details: {}", e.getClass().getSimpleName());
            if (e.getCause() != null) {
                logger.error("Caused by: {}", e.getCause().getMessage());
            }
            // In development, log the OTP instead of failing
            if ("dev".equalsIgnoreCase(activeProfile)) {
                logger.warn("DEV MODE: Email sending failed - OTP for {} is {}", toEmail, otp);
                logger.warn("Check your email configuration: EMAIL_HOST, EMAIL_PORT, EMAIL_USERNAME, EMAIL_PASSWORD");
            } else {
                // In production, we might want to throw or handle differently
                logger.error("Failed to send OTP email in production. User registration may fail.");
                throw new RuntimeException("Failed to send OTP email. Please try again later.", e);
            }
        }
    }

    private String buildOtpEmailBody(String otp) {
        return String.format("""
            Hello,
            
            Thank you for registering with Personal Finance Tracker!
            
            Your verification code is: %s
            
            This code will expire in 10 minutes.
            
            If you didn't request this code, please ignore this email.
            
            Best regards,
            Personal Finance Tracker Team
            """, otp);
    }

    public boolean isEmailEnabled() {
        return emailEnabled;
    }
}

