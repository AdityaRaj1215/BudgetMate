package com.personalfin.server.auth.dto;

public record PasswordValidationResponse(
        boolean isValid,
        String message
) {
    public static PasswordValidationResponse valid() {
        return new PasswordValidationResponse(true, "Password is valid");
    }

    public static PasswordValidationResponse invalid(String message) {
        return new PasswordValidationResponse(false, message);
    }
}

