package com.personalfin.server.security.service;

import com.personalfin.server.security.model.LoginAttempt;
import com.personalfin.server.security.repository.LoginAttemptRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class AccountLockoutService {

    private final LoginAttemptRepository loginAttemptRepository;

    @Value("${security.account-lockout.max-attempts:5}")
    private int maxAttempts;

    @Value("${security.account-lockout.lockout-duration-minutes:30}")
    private int lockoutDurationMinutes;

    @Value("${security.account-lockout.window-minutes:15}")
    private int windowMinutes;

    public AccountLockoutService(LoginAttemptRepository loginAttemptRepository) {
        this.loginAttemptRepository = loginAttemptRepository;
    }

    @Transactional
    public void recordFailedAttempt(String username, String ipAddress, String failureReason) {
        LoginAttempt attempt = new LoginAttempt();
        attempt.setUsername(username);
        attempt.setIpAddress(ipAddress);
        attempt.setSuccessful(false);
        attempt.setFailureReason(failureReason);
        loginAttemptRepository.save(attempt);
    }

    @Transactional
    public void recordSuccessfulAttempt(String username, String ipAddress) {
        LoginAttempt attempt = new LoginAttempt();
        attempt.setUsername(username);
        attempt.setIpAddress(ipAddress);
        attempt.setSuccessful(true);
        loginAttemptRepository.save(attempt);
    }

    public boolean isAccountLocked(String username) {
        OffsetDateTime windowStart = OffsetDateTime.now().minusMinutes(windowMinutes);
        long failedAttempts = loginAttemptRepository.countFailedAttemptsSince(username, windowStart);
        return failedAttempts >= maxAttempts;
    }

    public boolean isIpBlocked(String ipAddress) {
        OffsetDateTime windowStart = OffsetDateTime.now().minusMinutes(windowMinutes);
        long failedAttempts = loginAttemptRepository.countFailedAttemptsByIpSince(ipAddress, windowStart);
        return failedAttempts >= maxAttempts * 2; // IP blocking threshold is higher
    }

    public long getRemainingAttempts(String username) {
        OffsetDateTime windowStart = OffsetDateTime.now().minusMinutes(windowMinutes);
        long failedAttempts = loginAttemptRepository.countFailedAttemptsSince(username, windowStart);
        return Math.max(0, maxAttempts - failedAttempts);
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public int getLockoutDurationMinutes() {
        return lockoutDurationMinutes;
    }
}


