package com.personalfin.server.expense.dto;

import java.math.BigDecimal;

public record CategorySpendingSummary(
        String category,
        BigDecimal totalAmount,
        Long transactionCount,
        double percentageOfTotal
) {
}







