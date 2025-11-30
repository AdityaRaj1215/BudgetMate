package com.personalfin.server.expense.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseHeatmapPoint(
        LocalDate date,
        BigDecimal totalAmount,
        int level
) {
}

