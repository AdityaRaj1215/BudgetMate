package com.personalfin.server.expense.dto;

import java.util.Map;

public record ExpenseCategorizationResponse(
        String category,
        double confidence,
        String matchedKeyword,
        Map<String, Double> suggestions
) {
}

