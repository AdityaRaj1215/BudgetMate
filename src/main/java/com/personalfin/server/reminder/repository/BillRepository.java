package com.personalfin.server.reminder.repository;

import com.personalfin.server.reminder.model.Bill;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillRepository extends JpaRepository<Bill, UUID> {

    List<Bill> findByActiveTrueAndNextDueDateBetween(LocalDate start, LocalDate end);
}

