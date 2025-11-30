package com.personalfin.server.expense.service;

import com.personalfin.server.expense.dto.ExpenseCategorizationRequest;
import com.personalfin.server.expense.dto.ExpenseCategorizationResponse;

public interface ExpenseCategorizer {

    ExpenseCategorizationResponse categorize(ExpenseCategorizationRequest request);
}

