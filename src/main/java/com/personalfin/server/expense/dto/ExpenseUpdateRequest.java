package com.personalfin.server.expense.dto;

import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseUpdateRequest(
        String description,

        String merchant,

        @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than zero")
        BigDecimal amount,

        LocalDate transactionDate,

        String category,

        String paymentMethod
) {
    // All fields are optional for partial updates
}

