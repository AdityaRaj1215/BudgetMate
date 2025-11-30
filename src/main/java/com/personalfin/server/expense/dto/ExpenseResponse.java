package com.personalfin.server.expense.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ExpenseResponse(
        UUID id,
        String description,
        String merchant,
        String category,
        BigDecimal amount,
        LocalDate transactionDate,
        String paymentMethod,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}

