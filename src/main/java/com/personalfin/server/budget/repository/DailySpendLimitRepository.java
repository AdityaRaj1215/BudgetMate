package com.personalfin.server.budget.repository;

import com.personalfin.server.budget.model.DailySpendLimit;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DailySpendLimitRepository extends JpaRepository<DailySpendLimit, UUID> {

    Optional<DailySpendLimit> findByBudgetIdAndDate(UUID budgetId, LocalDate date);

    @Query("SELECT dsl FROM DailySpendLimit dsl WHERE dsl.budget.id = :budgetId AND dsl.date BETWEEN :start AND :end")
    List<DailySpendLimit> findByBudgetIdAndDateBetween(
            @Param("budgetId") UUID budgetId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    @Query("SELECT dsl FROM DailySpendLimit dsl WHERE dsl.date = :date")
    List<DailySpendLimit> findByDate(@Param("date") LocalDate date);
}










