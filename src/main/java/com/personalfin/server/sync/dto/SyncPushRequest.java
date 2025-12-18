package com.personalfin.server.sync.dto;

import com.personalfin.server.budget.dto.BudgetRequest;
import com.personalfin.server.expense.dto.ExpenseCreateRequest;
import com.personalfin.server.reminder.dto.BillRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record SyncPushRequest(
        @NotNull
        OffsetDateTime lastSyncAt,
        
        String deviceId,
        
        @Valid
        List<SyncEntity<ExpenseCreateRequest>> expenses,
        
        @Valid
        List<SyncEntity<BudgetRequest>> budgets,
        
        @Valid
        List<SyncEntity<BillRequest>> bills
) {
    
    public record SyncEntity<T>(
            UUID id,
            @NotNull
            String operation, // "create", "update", "delete"
            @NotNull
            OffsetDateTime clientUpdatedAt,
            T data
    ) {}
}



