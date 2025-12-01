package com.personalfin.server.budget.service;

import com.personalfin.server.budget.config.BudgetProperties;
import com.personalfin.server.budget.dto.CoachMessage;
import com.personalfin.server.budget.dto.DailySpendLimitResponse;
import com.personalfin.server.budget.repository.BudgetRepository;
import com.personalfin.server.expense.repository.ExpenseRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

@Service
public class DailySpendCoachService {

    private final BudgetService budgetService;
    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;
    private final BudgetProperties properties;
    private final Clock clock;

    public DailySpendCoachService(
            BudgetService budgetService,
            BudgetRepository budgetRepository,
            ExpenseRepository expenseRepository,
            BudgetProperties properties,
            Clock clock) {
        this.budgetService = budgetService;
        this.budgetRepository = budgetRepository;
        this.expenseRepository = expenseRepository;
        this.properties = properties;
        this.clock = clock;
    }

    public CoachMessage checkDailySpending(LocalDate date) {
        if (!properties.getCoach().isEnabled()) {
            return null;
        }

        try {
            DailySpendLimitResponse dailyLimit = budgetService.getDailyLimit(date);
            return evaluateSpending(dailyLimit);
        } catch (RuntimeException e) {
            // No active budget for this month
            return null;
        }
    }

    public CoachMessage checkTodaySpending() {
        return checkDailySpending(LocalDate.now(clock));
    }

    public CoachMessage evaluateSpendingAfterExpense(BigDecimal expenseAmount, LocalDate date) {
        if (!properties.getCoach().isEnabled()) {
            return null;
        }

        try {
            DailySpendLimitResponse dailyLimit = budgetService.getDailyLimit(date);
            BigDecimal newSpentAmount = dailyLimit.spentAmount().add(expenseAmount);
            
            // Create a temporary response with updated spent amount
            DailySpendLimitResponse updatedLimit = new DailySpendLimitResponse(
                    dailyLimit.id(),
                    dailyLimit.budgetId(),
                    dailyLimit.date(),
                    dailyLimit.dailyLimit(),
                    newSpentAmount,
                    dailyLimit.dailyLimit().subtract(newSpentAmount),
                    newSpentAmount.compareTo(dailyLimit.dailyLimit()) > 0
            );

            return evaluateSpending(updatedLimit);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private CoachMessage evaluateSpending(DailySpendLimitResponse dailyLimit) {
        BigDecimal spentAmount = dailyLimit.spentAmount();
        BigDecimal dailyLimitAmount = dailyLimit.dailyLimit();
        BigDecimal remaining = dailyLimit.remainingAmount();
        BigDecimal overspend = spentAmount.subtract(dailyLimitAmount);

        // Calculate percentage used
        double percentageUsed = spentAmount
                .divide(dailyLimitAmount, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();

        String message;
        CoachMessage.CoachMessageType type;

        if (overspend.compareTo(BigDecimal.ZERO) > 0) {
            // Overspent
            message = String.format(
                    "You overspent by ₹%.2f today. Your daily limit was ₹%.2f but you spent ₹%.2f.",
                    overspend, dailyLimitAmount, spentAmount
            );
            type = CoachMessage.CoachMessageType.CRITICAL;
        } else if (percentageUsed >= 90) {
            // Close to limit
            message = String.format(
                    "You've used %.1f%% of your daily limit (₹%.2f). Only ₹%.2f remaining.",
                    percentageUsed, dailyLimitAmount, remaining
            );
            type = CoachMessage.CoachMessageType.WARNING;
        } else if (percentageUsed >= 70) {
            // Approaching limit
            message = String.format(
                    "You've used %.1f%% of your daily limit. ₹%.2f remaining.",
                    percentageUsed, remaining
            );
            type = CoachMessage.CoachMessageType.WARNING;
        } else {
            // Within limit
            message = String.format(
                    "You've spent ₹%.2f today. ₹%.2f remaining from your daily limit of ₹%.2f.",
                    spentAmount, remaining, dailyLimitAmount
            );
            type = CoachMessage.CoachMessageType.INFO;
        }

        return new CoachMessage(
                message,
                type,
                overspend.compareTo(BigDecimal.ZERO) > 0 ? overspend : BigDecimal.ZERO,
                dailyLimitAmount,
                spentAmount
        );
    }
}









