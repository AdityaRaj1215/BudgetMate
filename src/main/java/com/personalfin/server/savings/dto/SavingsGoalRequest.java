package com.personalfin.server.savings.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record SavingsGoalRequest(
        @NotBlank(message = "Name is required")
        String name,

        @NotNull(message = "Target amount is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Target amount must be greater than zero")
        BigDecimal targetAmount,

        @FutureOrPresent(message = "Target date cannot be in the past")
        LocalDate targetDate
) {
}









