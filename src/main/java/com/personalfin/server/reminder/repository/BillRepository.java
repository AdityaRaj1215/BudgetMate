package com.personalfin.server.reminder.repository;

import com.personalfin.server.reminder.model.Bill;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillRepository extends JpaRepository<Bill, UUID> {

    List<Bill> findByActiveTrueAndNextDueDateBetween(LocalDate start, LocalDate end);

    @org.springframework.data.jpa.repository.Query("SELECT b FROM Bill b WHERE b.userId = :userId AND b.updatedAt > :since ORDER BY b.updatedAt ASC")
    List<Bill> findUpdatedSince(@Param("userId") java.util.UUID userId, @Param("since") java.time.OffsetDateTime since);

    List<Bill> findByUserId(java.util.UUID userId);
}

