package com.personalfin.server.sync.dto;

import com.personalfin.server.budget.dto.BudgetResponse;
import com.personalfin.server.expense.dto.ExpenseResponse;
import com.personalfin.server.reminder.dto.BillResponse;
import java.time.OffsetDateTime;
import java.util.List;

public record SyncPullResponse(
        OffsetDateTime serverSyncAt,
        OffsetDateTime lastSyncAt,
        int totalChanges,
        List<ExpenseResponse> expenses,
        List<BudgetResponse> budgets,
        List<BillResponse> bills,
        List<String> deletedExpenses,
        List<String> deletedBudgets,
        List<String> deletedBills
) {}



