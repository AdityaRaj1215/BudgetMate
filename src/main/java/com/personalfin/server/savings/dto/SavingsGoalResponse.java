package com.personalfin.server.savings.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record SavingsGoalResponse(
        UUID id,
        String name,
        BigDecimal targetAmount,
        BigDecimal currentAmount,
        BigDecimal remainingAmount,
        double progressPercentage,
        LocalDate targetDate,
        boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}










