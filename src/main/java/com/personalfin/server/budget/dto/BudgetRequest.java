package com.personalfin.server.budget.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record BudgetRequest(
        @NotBlank(message = "Name is required")
        String name,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than zero")
        BigDecimal amount,

        @NotNull(message = "Month/Year is required")
        LocalDate monthYear
) {
}










