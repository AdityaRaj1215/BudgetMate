package com.personalfin.server.security.exception;

public class AccountLockedException extends SecurityException {
    private final long remainingAttempts;
    private final int lockoutDurationMinutes;

    public AccountLockedException(String message, long remainingAttempts, int lockoutDurationMinutes) {
        super(message);
        this.remainingAttempts = remainingAttempts;
        this.lockoutDurationMinutes = lockoutDurationMinutes;
    }

    public long getRemainingAttempts() {
        return remainingAttempts;
    }

    public int getLockoutDurationMinutes() {
        return lockoutDurationMinutes;
    }
}



