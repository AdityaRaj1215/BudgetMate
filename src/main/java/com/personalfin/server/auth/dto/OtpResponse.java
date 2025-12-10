package com.personalfin.server.auth.dto;

public record OtpResponse(
        String message,
        int expiresInSeconds,
        String devOtp // Only populated in dev profile for testing
) {

    public static OtpResponse of(String message, int expiresInSeconds) {
        return new OtpResponse(message, expiresInSeconds, null);
    }

    public static OtpResponse ofWithDevOtp(String message, int expiresInSeconds, String devOtp) {
        return new OtpResponse(message, expiresInSeconds, devOtp);
    }
}

