package com.personalfin.server.expense.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record ExpenseCategorizationRequest(
        @NotBlank(message = "Description is required") String description,
        String merchant,
        @DecimalMin(value = "0.0", inclusive = true, message = "Amount cannot be negative")
        BigDecimal amount
) {
    public String normalizedMerchant() {
        return merchant == null ? "" : merchant;
    }
}

