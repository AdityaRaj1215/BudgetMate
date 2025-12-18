package com.personalfin.server.sync.service;

import com.personalfin.server.auth.util.SecurityUtils;
import com.personalfin.server.budget.dto.BudgetRequest;
import com.personalfin.server.budget.dto.BudgetResponse;
import com.personalfin.server.budget.model.Budget;
import com.personalfin.server.budget.repository.BudgetRepository;
import com.personalfin.server.budget.service.BudgetService;
import com.personalfin.server.expense.dto.ExpenseCreateRequest;
import com.personalfin.server.expense.dto.ExpenseResponse;
import com.personalfin.server.expense.model.Expense;
import com.personalfin.server.expense.repository.ExpenseRepository;
import com.personalfin.server.expense.service.ExpenseService;
import com.personalfin.server.reminder.dto.BillRequest;
import com.personalfin.server.reminder.dto.BillResponse;
import com.personalfin.server.reminder.model.Bill;
import com.personalfin.server.reminder.repository.BillRepository;
import com.personalfin.server.reminder.service.BillService;
import com.personalfin.server.sync.dto.SyncPullResponse;
import com.personalfin.server.sync.dto.SyncPushRequest;
import com.personalfin.server.sync.dto.SyncPushResponse;
import com.personalfin.server.sync.dto.SyncStatusResponse;
import com.personalfin.server.sync.model.SyncMetadata;
import com.personalfin.server.sync.repository.SyncMetadataRepository;
import com.personalfin.server.user.service.UserService;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class SyncService {

    private final SyncMetadataRepository syncMetadataRepository;
    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;
    private final BillRepository billRepository;
    private final ExpenseService expenseService;
    private final BudgetService budgetService;
    private final BillService billService;
    private final UserService userService;

    public SyncService(
            SyncMetadataRepository syncMetadataRepository,
            ExpenseRepository expenseRepository,
            BudgetRepository budgetRepository,
            BillRepository billRepository,
            ExpenseService expenseService,
            BudgetService budgetService,
            BillService billService,
            UserService userService) {
        this.syncMetadataRepository = syncMetadataRepository;
        this.expenseRepository = expenseRepository;
        this.budgetRepository = budgetRepository;
        this.billRepository = billRepository;
        this.expenseService = expenseService;
        this.budgetService = budgetService;
        this.billService = billService;
        this.userService = userService;
    }

    @Transactional
    public SyncPushResponse push(SyncPushRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId(userService);
        if (userId == null) {
            throw new IllegalStateException("User not authenticated");
        }

        OffsetDateTime serverSyncAt = OffsetDateTime.now(ZoneOffset.UTC);
        List<SyncPushResponse.SyncConflict> conflicts = new ArrayList<>();
        List<SyncPushResponse.SyncResult> results = new ArrayList<>();
        int processedCount = 0;

        // Process expenses
        if (request.expenses() != null) {
            for (SyncPushRequest.SyncEntity<ExpenseCreateRequest> entity : request.expenses()) {
                try {
                    SyncPushResponse.SyncResult result = processExpenseSync(entity, userId, conflicts);
                    results.add(result);
                    if (result.success()) {
                        processedCount++;
                    }
                } catch (Exception e) {
                    results.add(new SyncPushResponse.SyncResult(
                            "expense",
                            entity.id(),
                            entity.operation(),
                            false,
                            "Error: " + e.getMessage(),
                            null
                    ));
                }
            }
        }

        // Process budgets
        if (request.budgets() != null) {
            for (SyncPushRequest.SyncEntity<BudgetRequest> entity : request.budgets()) {
                try {
                    SyncPushResponse.SyncResult result = processBudgetSync(entity, userId, conflicts);
                    results.add(result);
                    if (result.success()) {
                        processedCount++;
                    }
                } catch (Exception e) {
                    results.add(new SyncPushResponse.SyncResult(
                            "budget",
                            entity.id(),
                            entity.operation(),
                            false,
                            "Error: " + e.getMessage(),
                            null
                    ));
                }
            }
        }

        // Process bills
        if (request.bills() != null) {
            for (SyncPushRequest.SyncEntity<BillRequest> entity : request.bills()) {
                try {
                    SyncPushResponse.SyncResult result = processBillSync(entity, userId, conflicts);
                    results.add(result);
                    if (result.success()) {
                        processedCount++;
                    }
                } catch (Exception e) {
                    results.add(new SyncPushResponse.SyncResult(
                            "bill",
                            entity.id(),
                            entity.operation(),
                            false,
                            "Error: " + e.getMessage(),
                            null
                    ));
                }
            }
        }

        // Update sync metadata
        updateSyncMetadata(userId, request.deviceId(), serverSyncAt);

        return new SyncPushResponse(
                serverSyncAt,
                processedCount,
                conflicts.size(),
                conflicts,
                results
        );
    }

    @Transactional
    public SyncPullResponse pull(OffsetDateTime lastSyncAt, String deviceId) {
        UUID userId = SecurityUtils.getCurrentUserId(userService);
        if (userId == null) {
            throw new IllegalStateException("User not authenticated");
        }

        OffsetDateTime serverSyncAt = OffsetDateTime.now(ZoneOffset.UTC);
        
        // If no lastSyncAt provided, get from metadata or use epoch
        if (lastSyncAt == null) {
            Optional<SyncMetadata> metadata = deviceId != null
                    ? syncMetadataRepository.findByUserIdAndDeviceId(userId, deviceId)
                    : syncMetadataRepository.findByUserId(userId);
            lastSyncAt = metadata.map(SyncMetadata::getLastSyncAt)
                    .orElse(OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC));
        }

        // Fetch updated entities
        List<Expense> updatedExpenses = expenseRepository.findUpdatedSince(userId, lastSyncAt);
        List<Budget> updatedBudgets = budgetRepository.findUpdatedSince(userId, lastSyncAt);
        List<Bill> updatedBills = billRepository.findUpdatedSince(userId, lastSyncAt);

        // Convert to DTOs
        List<ExpenseResponse> expenseResponses = updatedExpenses.stream()
                .map(this::toExpenseResponse)
                .collect(Collectors.toList());

        List<BudgetResponse> budgetResponses = updatedBudgets.stream()
                .map(this::toBudgetResponse)
                .collect(Collectors.toList());

        List<BillResponse> billResponses = updatedBills.stream()
                .map(this::toBillResponse)
                .collect(Collectors.toList());

        // For deleted entities, we'd need a soft delete or separate tracking
        // For now, we'll return empty lists for deleted entities
        // In a production system, you'd track deletions separately
        List<String> deletedExpenses = new ArrayList<>();
        List<String> deletedBudgets = new ArrayList<>();
        List<String> deletedBills = new ArrayList<>();

        int totalChanges = expenseResponses.size() + budgetResponses.size() + billResponses.size();

        // Update sync metadata
        updateSyncMetadata(userId, deviceId, serverSyncAt);

        return new SyncPullResponse(
                serverSyncAt,
                lastSyncAt,
                totalChanges,
                expenseResponses,
                budgetResponses,
                billResponses,
                deletedExpenses,
                deletedBudgets,
                deletedBills
        );
    }

    public SyncStatusResponse getStatus() {
        UUID userId = SecurityUtils.getCurrentUserId(userService);
        if (userId == null) {
            throw new IllegalStateException("User not authenticated");
        }

        Optional<SyncMetadata> metadata = syncMetadataRepository.findByUserId(userId);
        OffsetDateTime lastSyncAt = metadata.map(SyncMetadata::getLastSyncAt)
                .orElse(null);

        // For simplicity, we'll assume there are always pending changes
        // In production, you'd check if there are actual changes
        boolean hasUnsyncedChanges = true; // Could be enhanced to check actual changes
        int pendingChangesCount = 0; // Could be enhanced to count actual pending changes

        return new SyncStatusResponse(lastSyncAt, hasUnsyncedChanges, pendingChangesCount);
    }

    private SyncPushResponse.SyncResult processExpenseSync(
            SyncPushRequest.SyncEntity<ExpenseCreateRequest> entity,
            UUID userId,
            List<SyncPushResponse.SyncConflict> conflicts) {
        
        UUID entityId = entity.id();
        String operation = entity.operation();
        OffsetDateTime clientUpdatedAt = entity.clientUpdatedAt();

        Optional<Expense> existing = expenseRepository.findById(entityId);

        // Check for conflicts
        if (existing.isPresent()) {
            Expense expense = existing.get();
            // Verify ownership
            if (!expense.getUserId().equals(userId)) {
                throw new IllegalStateException("Expense does not belong to user");
            }

            // Conflict: server was updated after client's last sync
            if (expense.getUpdatedAt().isAfter(clientUpdatedAt) && !"delete".equals(operation)) {
                conflicts.add(new SyncPushResponse.SyncConflict(
                        "expense",
                        entityId,
                        "server_updated_after_client",
                        expense.getUpdatedAt(),
                        clientUpdatedAt
                ));
                return new SyncPushResponse.SyncResult(
                        "expense",
                        entityId,
                        operation,
                        false,
                        "Conflict: Server has newer version",
                        null
                );
            }
        } else if ("update".equals(operation) || "delete".equals(operation)) {
            conflicts.add(new SyncPushResponse.SyncConflict(
                    "expense",
                    entityId,
                    "entity_deleted_on_server",
                    null,
                    clientUpdatedAt
            ));
            return new SyncPushResponse.SyncResult(
                    "expense",
                    entityId,
                    operation,
                    false,
                    "Conflict: Entity not found on server",
                    null
            );
        }

        // Apply operation
        switch (operation) {
            case "create":
                if (existing.isPresent()) {
                    // Already exists, treat as update
                    return updateExpense(entityId, entity.data(), userId);
                } else {
                    ExpenseCreateRequest request = entity.data();
                    ExpenseResponse created = expenseService.createExpense(request);
                    return new SyncPushResponse.SyncResult(
                            "expense",
                            entityId,
                            operation,
                            true,
                            "Created successfully",
                            created.id()
                    );
                }
            case "update":
                return updateExpense(entityId, entity.data(), userId);
            case "delete":
                expenseRepository.deleteById(entityId);
                return new SyncPushResponse.SyncResult(
                        "expense",
                        entityId,
                        operation,
                        true,
                        "Deleted successfully",
                        null
                );
            default:
                throw new IllegalArgumentException("Unknown operation: " + operation);
        }
    }

    private SyncPushResponse.SyncResult updateExpense(UUID id, ExpenseCreateRequest data, UUID userId) {
        // Convert ExpenseCreateRequest to ExpenseUpdateRequest
        com.personalfin.server.expense.dto.ExpenseUpdateRequest updateRequest =
                new com.personalfin.server.expense.dto.ExpenseUpdateRequest(
                        data.description(),
                        data.merchant(),
                        data.category(),
                        data.amount(),
                        data.transactionDate(),
                        data.paymentMethod()
                );
        ExpenseResponse updated = expenseService.update(id, updateRequest);
        return new SyncPushResponse.SyncResult(
                "expense",
                id,
                "update",
                true,
                "Updated successfully",
                updated.id()
        );
    }

    private SyncPushResponse.SyncResult processBudgetSync(
            SyncPushRequest.SyncEntity<BudgetRequest> entity,
            UUID userId,
            List<SyncPushResponse.SyncConflict> conflicts) {
        
        UUID entityId = entity.id();
        String operation = entity.operation();
        OffsetDateTime clientUpdatedAt = entity.clientUpdatedAt();

        Optional<Budget> existing = budgetRepository.findById(entityId);

        // Check for conflicts
        if (existing.isPresent()) {
            Budget budget = existing.get();
            if (!budget.getUserId().equals(userId)) {
                throw new IllegalStateException("Budget does not belong to user");
            }

            if (budget.getUpdatedAt().isAfter(clientUpdatedAt) && !"delete".equals(operation)) {
                conflicts.add(new SyncPushResponse.SyncConflict(
                        "budget",
                        entityId,
                        "server_updated_after_client",
                        budget.getUpdatedAt(),
                        clientUpdatedAt
                ));
                return new SyncPushResponse.SyncResult(
                        "budget",
                        entityId,
                        operation,
                        false,
                        "Conflict: Server has newer version",
                        null
                );
            }
        } else if ("update".equals(operation) || "delete".equals(operation)) {
            conflicts.add(new SyncPushResponse.SyncConflict(
                    "budget",
                    entityId,
                    "entity_deleted_on_server",
                    null,
                    clientUpdatedAt
            ));
            return new SyncPushResponse.SyncResult(
                    "budget",
                    entityId,
                    operation,
                    false,
                    "Conflict: Entity not found on server",
                    null
            );
        }

        // Apply operation
        switch (operation) {
            case "create":
                if (existing.isPresent()) {
                    BudgetResponse updated = budgetService.update(entityId, entity.data());
                    return new SyncPushResponse.SyncResult(
                            "budget",
                            entityId,
                            "update",
                            true,
                            "Updated successfully",
                            updated.id()
                    );
                } else {
                    BudgetResponse created = budgetService.create(entity.data());
                    return new SyncPushResponse.SyncResult(
                            "budget",
                            entityId,
                            operation,
                            true,
                            "Created successfully",
                            created.id()
                    );
                }
            case "update":
                BudgetResponse updated = budgetService.update(entityId, entity.data());
                return new SyncPushResponse.SyncResult(
                        "budget",
                        entityId,
                        operation,
                        true,
                        "Updated successfully",
                        updated.id()
                );
            case "delete":
                budgetService.delete(entityId);
                return new SyncPushResponse.SyncResult(
                        "budget",
                        entityId,
                        operation,
                        true,
                        "Deleted successfully",
                        null
                );
            default:
                throw new IllegalArgumentException("Unknown operation: " + operation);
        }
    }

    private SyncPushResponse.SyncResult processBillSync(
            SyncPushRequest.SyncEntity<BillRequest> entity,
            UUID userId,
            List<SyncPushResponse.SyncConflict> conflicts) {
        
        UUID entityId = entity.id();
        String operation = entity.operation();
        OffsetDateTime clientUpdatedAt = entity.clientUpdatedAt();

        Optional<Bill> existing = billRepository.findById(entityId);

        // Check for conflicts
        if (existing.isPresent()) {
            Bill bill = existing.get();
            if (!bill.getUserId().equals(userId)) {
                throw new IllegalStateException("Bill does not belong to user");
            }

            if (bill.getUpdatedAt().isAfter(clientUpdatedAt) && !"delete".equals(operation)) {
                conflicts.add(new SyncPushResponse.SyncConflict(
                        "bill",
                        entityId,
                        "server_updated_after_client",
                        bill.getUpdatedAt(),
                        clientUpdatedAt
                ));
                return new SyncPushResponse.SyncResult(
                        "bill",
                        entityId,
                        operation,
                        false,
                        "Conflict: Server has newer version",
                        null
                );
            }
        } else if ("update".equals(operation) || "delete".equals(operation)) {
            conflicts.add(new SyncPushResponse.SyncConflict(
                    "bill",
                    entityId,
                    "entity_deleted_on_server",
                    null,
                    clientUpdatedAt
            ));
            return new SyncPushResponse.SyncResult(
                    "bill",
                    entityId,
                    operation,
                    false,
                    "Conflict: Entity not found on server",
                    null
            );
        }

        // Apply operation
        switch (operation) {
            case "create":
                if (existing.isPresent()) {
                    BillResponse updated = billService.update(entityId, entity.data());
                    return new SyncPushResponse.SyncResult(
                            "bill",
                            entityId,
                            "update",
                            true,
                            "Updated successfully",
                            updated.id()
                    );
                } else {
                    // Create bill and set userId manually
                    Bill bill = new Bill();
                    BillRequest request = entity.data();
                    bill.setName(request.name());
                    bill.setCategory(request.category());
                    bill.setAmount(request.amount());
                    bill.setNextDueDate(request.nextDueDate());
                    bill.setFrequency(request.frequency());
                    bill.setRemindDaysBefore(request.remindDaysBefore() != null ? request.remindDaysBefore() : 3);
                    bill.setActive(true);
                    bill.setUserId(userId);
                    Bill saved = billRepository.save(bill);
                    return new SyncPushResponse.SyncResult(
                            "bill",
                            entityId,
                            operation,
                            true,
                            "Created successfully",
                            saved.getId()
                    );
                }
            case "update":
                BillResponse updated = billService.update(entityId, entity.data());
                return new SyncPushResponse.SyncResult(
                        "bill",
                        entityId,
                        operation,
                        true,
                        "Updated successfully",
                        updated.id()
                );
            case "delete":
                billService.delete(entityId);
                return new SyncPushResponse.SyncResult(
                        "bill",
                        entityId,
                        operation,
                        true,
                        "Deleted successfully",
                        null
                );
            default:
                throw new IllegalArgumentException("Unknown operation: " + operation);
        }
    }

    private void updateSyncMetadata(UUID userId, String deviceId, OffsetDateTime syncAt) {
        Optional<SyncMetadata> existing = deviceId != null
                ? syncMetadataRepository.findByUserIdAndDeviceId(userId, deviceId)
                : syncMetadataRepository.findByUserId(userId);

        SyncMetadata metadata = existing.orElse(new SyncMetadata());
        metadata.setUserId(userId);
        metadata.setDeviceId(deviceId);
        metadata.setLastSyncAt(syncAt);
        syncMetadataRepository.save(metadata);
    }

    private ExpenseResponse toExpenseResponse(Expense expense) {
        return new ExpenseResponse(
                expense.getId(),
                expense.getDescription(),
                expense.getMerchant(),
                expense.getCategory(),
                expense.getAmount(),
                expense.getTransactionDate(),
                expense.getPaymentMethod(),
                expense.getCreatedAt(),
                expense.getUpdatedAt()
        );
    }

    private BudgetResponse toBudgetResponse(Budget budget) {
        return new BudgetResponse(
                budget.getId(),
                budget.getName(),
                budget.getAmount(),
                budget.getMonthYear(),
                budget.isActive(),
                budget.getCreatedAt(),
                budget.getUpdatedAt()
        );
    }

    private BillResponse toBillResponse(Bill bill) {
        return new BillResponse(
                bill.getId(),
                bill.getName(),
                bill.getCategory(),
                bill.getAmount(),
                bill.getNextDueDate(),
                bill.getFrequency(),
                bill.isActive(),
                bill.getRemindDaysBefore(),
                bill.getCreatedAt(),
                bill.getUpdatedAt()
        );
    }
}

