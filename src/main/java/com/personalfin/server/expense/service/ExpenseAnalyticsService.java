package com.personalfin.server.expense.service;

import com.personalfin.server.auth.util.SecurityUtils;
import com.personalfin.server.expense.dto.CategorySpendingSummary;
import com.personalfin.server.expense.dto.SpendingPattern;
import com.personalfin.server.expense.repository.ExpenseRepository;
import com.personalfin.server.user.service.UserService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ExpenseAnalyticsService {

    private final ExpenseRepository expenseRepository;
    private final UserService userService;

    public ExpenseAnalyticsService(ExpenseRepository expenseRepository, UserService userService) {
        this.expenseRepository = expenseRepository;
        this.userService = userService;
    }

    public List<CategorySpendingSummary> getCategorySpending(LocalDate start, LocalDate end) {
        UUID userId = SecurityUtils.getCurrentUserId(userService);
        List<ExpenseRepository.CategorySpendProjection> projections =
                expenseRepository.findCategorySums(userId, start, end);

        BigDecimal totalAmount = projections.stream()
                .map(ExpenseRepository.CategorySpendProjection::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return projections.stream()
                .map(p -> {
                    double percentage = totalAmount.compareTo(BigDecimal.ZERO) > 0
                            ? p.getTotal()
                                    .divide(totalAmount, 4, RoundingMode.HALF_UP)
                                    .multiply(BigDecimal.valueOf(100))
                                    .doubleValue()
                            : 0.0;

                    return new CategorySpendingSummary(
                            p.getCategory() != null ? p.getCategory() : "Uncategorized",
                            p.getTotal(),
                            p.getCount(),
                            percentage
                    );
                })
                .collect(Collectors.toList());
    }

    public List<SpendingPattern> getWeeklyPatterns(LocalDate start, LocalDate end) {
        UUID userId = SecurityUtils.getCurrentUserId(userService);
        List<ExpenseRepository.WeeklyPatternProjection> projections =
                expenseRepository.findWeeklyPatterns(userId, start, end);

        List<SpendingPattern> patterns = new ArrayList<>();
        String[] dayNames = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

        for (ExpenseRepository.WeeklyPatternProjection projection : projections) {
            int dayOfWeek = projection.getDayOfWeek();
            String dayName = dayNames[dayOfWeek];
            BigDecimal avgAmount = projection.getTotal()
                    .divide(BigDecimal.valueOf(projection.getCount()), 2, RoundingMode.HALF_UP);

            String description = String.format(
                    "You spend an average of ₹%.2f on %s (total: ₹%.2f across %d transactions)",
                    avgAmount, dayName, projection.getTotal(), projection.getCount()
            );

            patterns.add(new SpendingPattern(
                    "WEEKLY_" + dayName.toUpperCase(),
                    description,
                    avgAmount,
                    projection.getCount()
            ));
        }

        return patterns;
    }

    public List<SpendingPattern> getRecurringExpenses(LocalDate start, LocalDate end, String category) {
        UUID userId = SecurityUtils.getCurrentUserId(userService);
        List<ExpenseRepository.RecurringExpenseProjection> projections =
                expenseRepository.findRecurringExpenses(userId, start, end, category, 2L);

        return projections.stream()
                .map(p -> {
                    String description = String.format(
                            "You spent ₹%.2f on average at %s (%d times)",
                            p.getAvgAmount(), p.getMerchant() != null ? p.getMerchant() : "Unknown", p.getCount()
                    );

                    return new SpendingPattern(
                            "RECURRING_" + p.getCategory().toUpperCase(),
                            description,
                            p.getAvgAmount(),
                            p.getCount()
                    );
                })
                .collect(Collectors.toList());
    }

    public List<SpendingPattern> detectOverlaps(LocalDate start, LocalDate end) {
        List<SpendingPattern> patterns = new ArrayList<>();

        UUID userId = SecurityUtils.getCurrentUserId(userService);
        // Get all categories
        List<ExpenseRepository.CategorySpendProjection> categoryProjections =
                expenseRepository.findCategorySums(userId, start, end);

        for (ExpenseRepository.CategorySpendProjection categoryProj : categoryProjections) {
            if (categoryProj.getCategory() == null) {
                continue;
            }

            List<SpendingPattern> recurring = getRecurringExpenses(start, end, categoryProj.getCategory());
            patterns.addAll(recurring);
        }

        // Add weekly patterns
        patterns.addAll(getWeeklyPatterns(start, end));

        return patterns;
    }

    public Map<String, Object> getMonthlyComparison(LocalDate month1Start, LocalDate month1End,
                                                    LocalDate month2Start, LocalDate month2End) {
        List<CategorySpendingSummary> month1 = getCategorySpending(month1Start, month1End);
        List<CategorySpendingSummary> month2 = getCategorySpending(month2Start, month2End);

        BigDecimal month1Total = month1.stream()
                .map(CategorySpendingSummary::totalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal month2Total = month2.stream()
                .map(CategorySpendingSummary::totalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal difference = month2Total.subtract(month1Total);
        double percentageChange = month1Total.compareTo(BigDecimal.ZERO) > 0
                ? difference.divide(month1Total, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue()
                : 0.0;

        return Map.of(
                "month1Total", month1Total,
                "month2Total", month2Total,
                "difference", difference,
                "percentageChange", percentageChange,
                "month1Breakdown", month1,
                "month2Breakdown", month2
        );
    }
}





