package com.personalfin.server.budget.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record BudgetResponse(
        UUID id,
        String name,
        BigDecimal amount,
        LocalDate monthYear,
        boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}







