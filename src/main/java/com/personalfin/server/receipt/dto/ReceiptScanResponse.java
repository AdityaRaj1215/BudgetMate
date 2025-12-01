package com.personalfin.server.receipt.dto;

import com.personalfin.server.expense.dto.ExpenseCreateRequest;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ReceiptScanResponse(
        BigDecimal amount,
        String merchant,
        LocalDate date,
        String category,
        String rawText,
        ExpenseCreateRequest expenseRequest
) {
}









