package com.personalfin.server.reminder.repository;

import com.personalfin.server.reminder.model.Bill;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BillRepository extends JpaRepository<Bill, UUID> {

    List<Bill> findByActiveTrueAndNextDueDateBetween(LocalDate start, LocalDate end);

    @Query("SELECT b FROM Bill b WHERE b.userId = :userId AND b.updatedAt > :since ORDER BY b.updatedAt ASC")
    List<Bill> findUpdatedSince(@Param("userId") UUID userId, @Param("since") OffsetDateTime since);

    List<Bill> findByUserId(UUID userId);
}
