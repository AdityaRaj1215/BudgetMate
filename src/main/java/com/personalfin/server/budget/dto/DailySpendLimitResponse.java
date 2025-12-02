package com.personalfin.server.budget.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record DailySpendLimitResponse(
        UUID id,
        UUID budgetId,
        LocalDate date,
        BigDecimal dailyLimit,
        BigDecimal spentAmount,
        BigDecimal remainingAmount,
        boolean overspent
) {
}










