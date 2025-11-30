package com.personalfin.server.budget.repository;

import com.personalfin.server.budget.model.Budget;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BudgetRepository extends JpaRepository<Budget, UUID> {

    @Query("SELECT b FROM Budget b WHERE b.monthYear = :monthYear AND b.active = true")
    Optional<Budget> findActiveByMonthYear(@Param("monthYear") LocalDate monthYear);

    @Query("SELECT b FROM Budget b WHERE b.active = true ORDER BY b.monthYear DESC")
    List<Budget> findAllActive();

    @Query("SELECT b FROM Budget b WHERE b.monthYear BETWEEN :start AND :end AND b.active = true")
    List<Budget> findActiveBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);
}







