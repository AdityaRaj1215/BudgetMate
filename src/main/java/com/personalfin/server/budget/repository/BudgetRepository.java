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

    @Query("SELECT b FROM Budget b WHERE b.monthYear = :monthYear AND b.userId = :userId AND b.active = true")
    Optional<Budget> findActiveByMonthYearAndUserId(@Param("monthYear") LocalDate monthYear, @Param("userId") UUID userId);

    @Query("SELECT b FROM Budget b WHERE b.monthYear = :monthYear AND b.active = true")
    Optional<Budget> findActiveByMonthYear(@Param("monthYear") LocalDate monthYear);

    @Query("SELECT b FROM Budget b WHERE b.userId = :userId AND b.active = true ORDER BY b.monthYear DESC")
    List<Budget> findAllActiveByUserId(@Param("userId") UUID userId);

    @Query("SELECT b FROM Budget b WHERE b.active = true ORDER BY b.monthYear DESC")
    List<Budget> findAllActive();

    @Query("SELECT b FROM Budget b WHERE b.monthYear BETWEEN :start AND :end AND b.userId = :userId AND b.active = true")
    List<Budget> findActiveBetweenByUserId(@Param("userId") UUID userId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT b FROM Budget b WHERE b.monthYear BETWEEN :start AND :end AND b.active = true")
    List<Budget> findActiveBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT b FROM Budget b WHERE b.userId = :userId AND b.updatedAt > :since ORDER BY b.updatedAt ASC")
    List<Budget> findUpdatedSince(@Param("userId") UUID userId, @Param("since") java.time.OffsetDateTime since);

    List<Budget> findByUserId(UUID userId);
}







