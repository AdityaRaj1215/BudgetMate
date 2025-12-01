package com.personalfin.server.budget.service;

import com.personalfin.server.auth.util.SecurityUtils;
import com.personalfin.server.budget.dto.BudgetRequest;
import com.personalfin.server.budget.dto.BudgetResponse;
import com.personalfin.server.budget.dto.DailySpendLimitResponse;
import com.personalfin.server.budget.model.Budget;
import com.personalfin.server.budget.model.DailySpendLimit;
import com.personalfin.server.budget.repository.BudgetRepository;
import com.personalfin.server.budget.repository.DailySpendLimitRepository;
import com.personalfin.server.expense.repository.ExpenseRepository;
import com.personalfin.server.user.service.UserService;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final DailySpendLimitRepository dailySpendLimitRepository;
    private final ExpenseRepository expenseRepository;
    private final UserService userService;
    private final Clock clock;

    public BudgetService(
            BudgetRepository budgetRepository,
            DailySpendLimitRepository dailySpendLimitRepository,
            ExpenseRepository expenseRepository,
            UserService userService,
            Clock clock) {
        this.budgetRepository = budgetRepository;
        this.dailySpendLimitRepository = dailySpendLimitRepository;
        this.expenseRepository = expenseRepository;
        this.userService = userService;
        this.clock = clock;
    }

    @Transactional
    public BudgetResponse create(BudgetRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId(userService);
        if (userId == null) {
            throw new IllegalStateException("User not authenticated");
        }
        
        LocalDate monthYear = request.monthYear().withDayOfMonth(1);
        
        // Deactivate any existing budget for the same month for this user
        budgetRepository.findActiveByMonthYearAndUserId(monthYear, userId)
                .ifPresent(existing -> existing.setActive(false));

        Budget budget = new Budget();
        budget.setName(request.name());
        budget.setAmount(request.amount());
        budget.setMonthYear(monthYear);
        budget.setUserId(userId);
        budget.setActive(true);

        Budget saved = budgetRepository.save(budget);
        generateDailyLimits(saved);

        return toResponse(saved);
    }

    @Transactional
    public BudgetResponse update(UUID id, BudgetRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId(userService);
        if (userId == null) {
            throw new IllegalStateException("User not authenticated");
        }
        
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget not found: " + id));
        
        // Verify the budget belongs to the current user
        if (!budget.getUserId().equals(userId)) {
            throw new IllegalStateException("Budget does not belong to the current user");
        }

        LocalDate monthYear = request.monthYear().withDayOfMonth(1);
        budget.setName(request.name());
        budget.setAmount(request.amount());
        
        // If month changed, update monthYear and regenerate daily limits
        if (!budget.getMonthYear().equals(monthYear)) {
            budget.setMonthYear(monthYear);
            // Delete old daily limits
            dailySpendLimitRepository.findByBudgetIdAndDateBetween(
                    budget.getId(),
                    budget.getMonthYear(),
                    budget.getMonthYear().plusMonths(1).minusDays(1)
            ).forEach(dailySpendLimitRepository::delete);
            generateDailyLimits(budget);
        } else {
            // Recalculate daily limits for the same month
            regenerateDailyLimits(budget);
        }

        return toResponse(budget);
    }

    @Transactional
    public void delete(UUID id) {
        UUID userId = SecurityUtils.getCurrentUserId(userService);
        if (userId == null) {
            throw new IllegalStateException("User not authenticated");
        }
        
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget not found: " + id));
        
        // Verify the budget belongs to the current user
        if (!budget.getUserId().equals(userId)) {
            throw new IllegalStateException("Budget does not belong to the current user");
        }
        
        budgetRepository.delete(budget);
    }

    @Transactional
    public BudgetResponse deactivate(UUID id) {
        UUID userId = SecurityUtils.getCurrentUserId(userService);
        if (userId == null) {
            throw new IllegalStateException("User not authenticated");
        }
        
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget not found: " + id));
        
        // Verify the budget belongs to the current user
        if (!budget.getUserId().equals(userId)) {
            throw new IllegalStateException("Budget does not belong to the current user");
        }
        
        budget.setActive(false);
        return toResponse(budget);
    }

    public List<BudgetResponse> list() {
        UUID userId = SecurityUtils.getCurrentUserId(userService);
        if (userId == null) {
            throw new IllegalStateException("User not authenticated");
        }
        
        return budgetRepository.findAllActiveByUserId(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public BudgetResponse getById(UUID id) {
        UUID userId = SecurityUtils.getCurrentUserId(userService);
        if (userId == null) {
            throw new IllegalStateException("User not authenticated");
        }
        
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget not found: " + id));
        
        // Verify the budget belongs to the current user
        if (!budget.getUserId().equals(userId)) {
            throw new IllegalStateException("Budget does not belong to the current user");
        }
        
        return toResponse(budget);
    }

    public BudgetResponse getCurrentBudget() {
        UUID userId = SecurityUtils.getCurrentUserId(userService);
        if (userId == null) {
            throw new IllegalStateException("User not authenticated");
        }
        
        LocalDate today = LocalDate.now(clock);
        LocalDate monthStart = today.withDayOfMonth(1);
        
        return budgetRepository.findActiveByMonthYearAndUserId(monthStart, userId)
                .map(this::toResponse)
                .orElse(null);
    }

    public DailySpendLimitResponse getDailyLimit(LocalDate date) {
        UUID userId = SecurityUtils.getCurrentUserId(userService);
        if (userId == null) {
            throw new IllegalStateException("User not authenticated");
        }
        
        LocalDate monthStart = date.withDayOfMonth(1);
        Budget budget = budgetRepository.findActiveByMonthYearAndUserId(monthStart, userId)
                .orElse(null);
        
        if (budget == null) {
            // Return a response indicating no budget exists
            BigDecimal spentAmount = expenseRepository.findDailySums(userId, date, date)
                    .stream()
                    .findFirst()
                    .map(ExpenseRepository.DailySpendProjection::getTotal)
                    .orElse(BigDecimal.ZERO);
            
            return new DailySpendLimitResponse(
                    null, // no daily limit ID
                    null, // no budget ID
                    date,
                    BigDecimal.ZERO, // no daily limit
                    spentAmount,
                    BigDecimal.ZERO, // no remaining amount
                    false // not overspent
            );
        }

        DailySpendLimit dailyLimit = dailySpendLimitRepository
                .findByBudgetIdAndDate(budget.getId(), date)
                .orElseGet(() -> {
                    // Generate on the fly if not exists
                    DailySpendLimit newLimit = createDailyLimit(budget, date);
                    return dailySpendLimitRepository.save(newLimit);
                });

        BigDecimal spentAmount = expenseRepository.findDailySums(userId, date, date)
                .stream()
                .findFirst()
                .map(ExpenseRepository.DailySpendProjection::getTotal)
                .orElse(BigDecimal.ZERO);

        BigDecimal remainingAmount = dailyLimit.getDailyLimit().subtract(spentAmount);
        boolean overspent = spentAmount.compareTo(dailyLimit.getDailyLimit()) > 0;

        return new DailySpendLimitResponse(
                dailyLimit.getId(),
                budget.getId(),
                date,
                dailyLimit.getDailyLimit(),
                spentAmount,
                remainingAmount,
                overspent
        );
    }

    private void generateDailyLimits(Budget budget) {
        YearMonth yearMonth = YearMonth.from(budget.getMonthYear());
        LocalDate start = budget.getMonthYear();
        LocalDate end = yearMonth.atEndOfMonth();

        LocalDate current = start;
        while (!current.isAfter(end)) {
            DailySpendLimit dailyLimit = createDailyLimit(budget, current);
            dailySpendLimitRepository.save(dailyLimit);
            current = current.plusDays(1);
        }
    }

    private void regenerateDailyLimits(Budget budget) {
        YearMonth yearMonth = YearMonth.from(budget.getMonthYear());
        LocalDate start = budget.getMonthYear();
        LocalDate end = yearMonth.atEndOfMonth();

        List<DailySpendLimit> existing = dailySpendLimitRepository
                .findByBudgetIdAndDateBetween(budget.getId(), start, end);

        // Delete existing
        existing.forEach(dailySpendLimitRepository::delete);

        // Regenerate
        generateDailyLimits(budget);
    }

    private DailySpendLimit createDailyLimit(Budget budget, LocalDate date) {
        YearMonth yearMonth = YearMonth.from(date);
        int daysInMonth = yearMonth.lengthOfMonth();
        BigDecimal dailyLimit = budget.getAmount()
                .divide(BigDecimal.valueOf(daysInMonth), 2, RoundingMode.HALF_UP);

        DailySpendLimit limit = new DailySpendLimit();
        limit.setBudget(budget);
        limit.setDate(date);
        limit.setDailyLimit(dailyLimit);
        return limit;
    }

    private BudgetResponse toResponse(Budget budget) {
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
}







