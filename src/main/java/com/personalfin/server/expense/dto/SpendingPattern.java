package com.personalfin.server.expense.dto;

import java.math.BigDecimal;

public record SpendingPattern(
        String pattern,
        String description,
        BigDecimal averageAmount,
        Long occurrenceCount
) {
}









