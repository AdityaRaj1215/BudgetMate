package com.personalfin.server.expense.repository;

import com.personalfin.server.expense.model.Expense;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

    List<Expense> findByUserId(UUID userId);

    @Query("select e.transactionDate as date, SUM(e.amount) as total "
            + "from Expense e where e.userId = :userId and e.transactionDate between :start and :end "
            + "group by e.transactionDate")
    List<DailySpendProjection> findDailySums(
            @Param("userId") UUID userId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    @Query("select e.category as category, SUM(e.amount) as total, COUNT(e) as count "
            + "from Expense e where e.userId = :userId and e.transactionDate between :start and :end "
            + "group by e.category")
    List<CategorySpendProjection> findCategorySums(
            @Param("userId") UUID userId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    @Query(value = "select EXTRACT(DOW FROM e.transaction_date) as dayOfWeek, SUM(e.amount) as total, COUNT(e) as count "
            + "from expenses e where e.user_id = :userId and e.transaction_date between :start and :end "
            + "group by EXTRACT(DOW FROM e.transaction_date)", nativeQuery = true)
    List<WeeklyPatternProjection> findWeeklyPatterns(
            @Param("userId") UUID userId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    @Query("select e.category as category, e.merchant as merchant, AVG(e.amount) as avgAmount, COUNT(e) as count "
            + "from Expense e where e.userId = :userId and e.transactionDate between :start and :end "
            + "and e.category = :category "
            + "group by e.category, e.merchant "
            + "having COUNT(e) >= :minOccurrences")
    List<RecurringExpenseProjection> findRecurringExpenses(
            @Param("userId") UUID userId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            @Param("category") String category,
            @Param("minOccurrences") long minOccurrences);

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.userId = :userId AND e.transactionDate = :date")
    java.math.BigDecimal findTotalSpentOnDate(@Param("userId") UUID userId, @Param("date") LocalDate date);

    interface DailySpendProjection {
        LocalDate getDate();
        java.math.BigDecimal getTotal();
    }

    interface CategorySpendProjection {
        String getCategory();
        java.math.BigDecimal getTotal();
        Long getCount();
    }

    interface WeeklyPatternProjection {
        Integer getDayOfWeek();
        java.math.BigDecimal getTotal();
        Long getCount();
    }

    interface RecurringExpenseProjection {
        String getCategory();
        String getMerchant();
        java.math.BigDecimal getAvgAmount();
        Long getCount();
    }
}

