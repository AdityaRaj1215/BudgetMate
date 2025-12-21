package com.personalfin.server.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record PasswordValidationRequest(
        @NotBlank(message = "Password is required")
        String password
) {
}

