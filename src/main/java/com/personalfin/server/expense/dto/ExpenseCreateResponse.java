package com.personalfin.server.expense.dto;

import com.personalfin.server.budget.dto.CoachMessage;

public record ExpenseCreateResponse(
        ExpenseResponse expense,
        CoachMessage coachMessage
) {
}










