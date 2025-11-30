package com.personalfin.server.expense.service;

import com.personalfin.server.auth.util.SecurityUtils;
import com.personalfin.server.budget.dto.CoachMessage;
import com.personalfin.server.budget.service.DailySpendCoachService;
import com.personalfin.server.expense.config.ExpenseAnalyticsProperties;
import com.personalfin.server.expense.dto.ExpenseCreateRequest;
import com.personalfin.server.expense.dto.ExpenseCreateResponse;
import com.personalfin.server.expense.dto.ExpenseHeatmapPoint;
import com.personalfin.server.expense.dto.ExpenseResponse;
import com.personalfin.server.expense.dto.ExpenseCategorizationResponse;
import com.personalfin.server.expense.model.Expense;
import com.personalfin.server.expense.repository.ExpenseRepository;
import com.personalfin.server.user.service.UserService;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseCategorizer expenseCategorizer;
    private final ExpenseAnalyticsProperties analyticsProperties;
    private final DailySpendCoachService coachService;
    private final UserService userService;
    private final Clock clock;

    public ExpenseService(ExpenseRepository expenseRepository,
                          ExpenseCategorizer expenseCategorizer,
                          ExpenseAnalyticsProperties analyticsProperties,
                          DailySpendCoachService coachService,
                          UserService userService,
                          Clock clock) {
        this.expenseRepository = expenseRepository;
        this.expenseCategorizer = expenseCategorizer;
        this.analyticsProperties = analyticsProperties;
        this.coachService = coachService;
        this.userService = userService;
        this.clock = clock;
    }

    @Transactional
    public ExpenseResponse createExpense(ExpenseCreateRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId(userService);
        Expense expense = new Expense();
        expense.setDescription(request.description());
        expense.setMerchant(request.merchant());
        expense.setAmount(request.amount());
        expense.setTransactionDate(request.transactionDate() != null
                ? request.transactionDate()
                : LocalDate.now(clock));
        expense.setPaymentMethod(request.paymentMethod());
        expense.setCategory(resolveCategory(request));
        expense.setUserId(userId);
        Expense saved = expenseRepository.save(expense);
        return toResponse(saved);
    }

    @Transactional
    public ExpenseCreateResponse createExpenseWithCoach(ExpenseCreateRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId(userService);
        LocalDate transactionDate = request.transactionDate() != null
                ? request.transactionDate()
                : LocalDate.now(clock);
        
        Expense expense = new Expense();
        expense.setDescription(request.description());
        expense.setMerchant(request.merchant());
        expense.setAmount(request.amount());
        expense.setTransactionDate(transactionDate);
        expense.setPaymentMethod(request.paymentMethod());
        expense.setCategory(resolveCategory(request));
        expense.setUserId(userId);
        Expense saved = expenseRepository.save(expense);
        
        // Check budget and get coach message
        CoachMessage coachMessage = coachService.evaluateSpendingAfterExpense(
                request.amount(), transactionDate);
        
        return new ExpenseCreateResponse(toResponse(saved), coachMessage);
    }

    public List<ExpenseResponse> listAll() {
        UUID userId = SecurityUtils.getCurrentUserId(userService);
        return expenseRepository.findByUserId(userId)
                .stream()
                .sorted(Comparator.comparing(Expense::getTransactionDate).reversed())
                .map(this::toResponse)
                .toList();
    }

    public List<ExpenseHeatmapPoint> heatmap(LocalDate start, LocalDate end) {
        UUID userId = SecurityUtils.getCurrentUserId(userService);
        List<ExpenseRepository.DailySpendProjection> projections = expenseRepository.findDailySums(userId, start, end);
        return projections.stream()
                .map(p -> new ExpenseHeatmapPoint(
                        p.getDate(),
                        p.getTotal(),
                        determineLevel(p.getTotal())))
                .sorted(Comparator.comparing(ExpenseHeatmapPoint::date))
                .toList();
    }

    private String resolveCategory(ExpenseCreateRequest request) {
        if (request.category() != null && !request.category().isBlank()) {
            return request.category();
        }
        ExpenseCategorizationResponse response = expenseCategorizer.categorize(
                new com.personalfin.server.expense.dto.ExpenseCategorizationRequest(
                        request.description(),
                        request.merchant(),
                        request.amount()
                ));
        return response.category();
    }

    private ExpenseResponse toResponse(Expense expense) {
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

    private int determineLevel(BigDecimal total) {
        if (total == null || total.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        List<BigDecimal> thresholds = analyticsProperties.getHeatmap().getThresholds();
        for (int i = 0; i < thresholds.size(); i++) {
            if (total.compareTo(thresholds.get(i)) <= 0) {
                return i + 1;
            }
        }
        return thresholds.size() + 1;
    }
}

