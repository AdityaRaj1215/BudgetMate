package com.personalfin.server.auth.service;

import com.personalfin.server.auth.dto.OtpResponse;
import com.personalfin.server.auth.model.OtpCode;
import com.personalfin.server.auth.repository.OtpCodeRepository;
import com.personalfin.server.email.EmailService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.Optional;
import java.util.Random;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OtpService {

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 10;
    private static final int MIN_SECONDS_BETWEEN_REQUESTS = 60;

    private final OtpCodeRepository otpCodeRepository;
    private final EmailService emailService;
    private final Random random = new Random();

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    public OtpService(OtpCodeRepository otpCodeRepository, EmailService emailService) {
        this.otpCodeRepository = otpCodeRepository;
        this.emailService = emailService;
    }

    @Transactional
    public OtpResponse generateOtp(String email) {
        // Throttle OTP requests per email
        Optional<OtpCode> latest = otpCodeRepository.findFirstByEmailOrderByCreatedAtDesc(email.toLowerCase());
        if (latest.isPresent()) {
            OffsetDateTime lastCreated = latest.get().getCreatedAt();
            if (lastCreated.isAfter(OffsetDateTime.now().minusSeconds(MIN_SECONDS_BETWEEN_REQUESTS))) {
                throw new IllegalArgumentException("Please wait before requesting another OTP");
            }
        }

        String otp = generateNumericOtp();
        String hash = hashOtp(email, otp);

        OtpCode otpCode = new OtpCode();
        otpCode.setEmail(email.toLowerCase());
        otpCode.setCodeHash(hash);
        otpCode.setExpiresAt(OffsetDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        otpCodeRepository.save(otpCode);

        // Send OTP via email
        emailService.sendOtpEmail(email.toLowerCase(), otp);

        // In development mode, also return OTP in response for easier testing
        // In production, only return success message
        if ("dev".equalsIgnoreCase(activeProfile) && !emailService.isEmailEnabled()) {
            return OtpResponse.ofWithDevOtp(
                    "OTP generated successfully (Email disabled - Dev mode)",
                    OTP_EXPIRY_MINUTES * 60,
                    otp
            );
        }

        return OtpResponse.of("OTP sent successfully to your email", OTP_EXPIRY_MINUTES * 60);
    }

    @Transactional
    public void verifyOtp(String email, String otp) {
        if (otp == null || !otp.matches("\\d{" + OTP_LENGTH + "}")) {
            throw new IllegalArgumentException("Invalid OTP format");
        }

        OtpCode active = otpCodeRepository.findActiveByEmail(email.toLowerCase(), OffsetDateTime.now())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired OTP"));

        String hash = hashOtp(email, otp);
        if (!hash.equals(active.getCodeHash())) {
            throw new IllegalArgumentException("Invalid or expired OTP");
        }

        active.setUsed(true);
        active.setUsedAt(OffsetDateTime.now());
        otpCodeRepository.save(active);
    }

    private String generateNumericOtp() {
        int bound = (int) Math.pow(10, OTP_LENGTH);
        int code = random.nextInt(bound);
        return String.format("%0" + OTP_LENGTH + "d", code);
    }

    private String hashOtp(String email, String otp) {
        String data = email.toLowerCase() + ":" + otp;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Unable to hash OTP", e);
        }
    }
}

