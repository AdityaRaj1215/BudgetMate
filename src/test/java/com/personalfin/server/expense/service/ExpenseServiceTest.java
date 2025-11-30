package com.personalfin.server.expense.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.personalfin.server.expense.config.ExpenseAnalyticsProperties;
import com.personalfin.server.expense.dto.ExpenseCategorizationResponse;
import com.personalfin.server.expense.dto.ExpenseCreateRequest;
import com.personalfin.server.expense.dto.ExpenseHeatmapPoint;
import com.personalfin.server.expense.dto.ExpenseResponse;
import com.personalfin.server.expense.model.Expense;
import com.personalfin.server.expense.repository.ExpenseRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private ExpenseCategorizer expenseCategorizer;

    private ExpenseService expenseService;

    private Clock clock;

    @BeforeEach
    void setUp() {
        clock = Clock.systemUTC();
        expenseService = new ExpenseService(
                expenseRepository,
                expenseCategorizer,
                new ExpenseAnalyticsProperties(),
                clock);
    }

    @Test
    void shouldAutoCategorizeWhenCategoryMissing() {
        ExpenseCreateRequest request = new ExpenseCreateRequest(
                "Lunch with friends",
                "Cafe",
                BigDecimal.valueOf(750),
                LocalDate.now(),
                null,
                "Card"
        );

        when(expenseCategorizer.categorize(any())).thenReturn(
                new ExpenseCategorizationResponse("Food", 0.9, "cafe", Map.of()));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> {
            Expense expense = invocation.getArgument(0);
            expense.setId(UUID.randomUUID());
            return expense;
        });

        ExpenseResponse response = expenseService.createExpense(request);

        assertThat(response.category()).isEqualTo("Food");
    }

    @Test
    void shouldMapHeatmapLevels() {
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 1, 5);
        ExpenseRepository.DailySpendProjection low = new ProjectionStub(start.plusDays(1), new BigDecimal("200"));
        ExpenseRepository.DailySpendProjection medium = new ProjectionStub(start.plusDays(2), new BigDecimal("1600"));
        ExpenseRepository.DailySpendProjection high = new ProjectionStub(start.plusDays(3), new BigDecimal("7000"));

        when(expenseRepository.findDailySums(start, end))
                .thenReturn(List.of(low, medium, high));

        List<ExpenseHeatmapPoint> result = expenseService.heatmap(start, end);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).level()).isEqualTo(1);
        assertThat(result.get(1).level()).isEqualTo(3);
        assertThat(result.get(2).level()).isEqualTo(5);
    }

    private static class ProjectionStub implements ExpenseRepository.DailySpendProjection {
        private final LocalDate date;
        private final BigDecimal total;

        ProjectionStub(LocalDate date, BigDecimal total) {
            this.date = date;
            this.total = total;
        }

        @Override
        public LocalDate getDate() {
            return date;
        }

        @Override
        public BigDecimal getTotal() {
            return total;
        }
    }
}

