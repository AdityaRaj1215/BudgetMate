package com.personalfin.server.expense.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseFilterRequest(
        // Date range filters
        LocalDate startDate,
        LocalDate endDate,
        
        // Category filter
        String category,
        
        // Amount range filters
        BigDecimal minAmount,
        BigDecimal maxAmount,
        
        // Search text (searches in description and merchant)
        String search,
        
        // Payment method filter
        String paymentMethod
) {
    // All fields are optional - if null, that filter is not applied
}




