package com.personalfin.server.security.validation;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class PasswordValidator {

    // Password must be at least 8 characters, contain at least one lowercase, one digit, and one special character
    // Allowed special characters: @$!%*?&#^_+-=()[]{}|\\:;"'<>,./~`
    // Using a simpler approach: check for at least one non-alphanumeric character
    private static final Pattern HAS_LOWERCASE = Pattern.compile(".*[a-z].*");
    private static final Pattern HAS_DIGIT = Pattern.compile(".*\\d.*");
    private static final Pattern HAS_SPECIAL_CHAR = Pattern.compile(".*[^A-Za-z0-9].*");
    private static final Pattern VALID_CHARS = Pattern.compile("^[A-Za-z0-9@$!%*?&#^_+\\-=()\\[\\]{}|\\\\:;\"'<>,./~`]+$");

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 128;

    public PasswordValidationResult validate(String password) {
        if (password == null || password.isBlank()) {
            return PasswordValidationResult.invalid("Password cannot be empty");
        }

        if (password.length() < MIN_LENGTH) {
            return PasswordValidationResult.invalid(
                    String.format("Password must be at least %d characters long", MIN_LENGTH)
            );
        }

        if (password.length() > MAX_LENGTH) {
            return PasswordValidationResult.invalid(
                    String.format("Password must not exceed %d characters", MAX_LENGTH)
            );
        }

        // Check individual requirements
        if (!HAS_LOWERCASE.matcher(password).matches()) {
            return PasswordValidationResult.invalid(
                    "Password must contain at least one lowercase letter"
            );
        }

        if (!HAS_DIGIT.matcher(password).matches()) {
            return PasswordValidationResult.invalid(
                    "Password must contain at least one digit"
            );
        }

        if (!HAS_SPECIAL_CHAR.matcher(password).matches()) {
            return PasswordValidationResult.invalid(
                    "Password must contain at least one special character (e.g., @$!%*?&#^_+-=()[]{}|\\:;\"'<>,./~`)"
            );
        }

        // Check for invalid characters (only allow alphanumeric and common special characters)
        if (!VALID_CHARS.matcher(password).matches()) {
            return PasswordValidationResult.invalid(
                    "Password contains invalid characters. Only letters, numbers, and common special characters are allowed"
            );
        }

        // Check for common weak passwords
        if (isCommonPassword(password)) {
            return PasswordValidationResult.invalid("Password is too common. Please choose a stronger password");
        }

        return PasswordValidationResult.valid();
    }

    private boolean isCommonPassword(String password) {
        String[] commonPasswords = {
                "password", "password123", "12345678", "qwerty123", "admin123",
                "letmein", "welcome", "monkey", "1234567890", "abc123"
        };
        String lowerPassword = password.toLowerCase();
        for (String common : commonPasswords) {
            if (lowerPassword.contains(common)) {
                return true;
            }
        }
        return false;
    }

    public static class PasswordValidationResult {
        private final boolean valid;
        private final String errorMessage;

        private PasswordValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public static PasswordValidationResult valid() {
            return new PasswordValidationResult(true, null);
        }

        public static PasswordValidationResult invalid(String errorMessage) {
            return new PasswordValidationResult(false, errorMessage);
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}



