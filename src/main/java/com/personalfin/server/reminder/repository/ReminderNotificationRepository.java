package com.personalfin.server.reminder.repository;

import com.personalfin.server.reminder.model.ReminderNotificationLog;
import com.personalfin.server.reminder.model.ReminderType;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReminderNotificationRepository extends JpaRepository<ReminderNotificationLog, UUID> {

    boolean existsByBill_IdAndNotificationDateAndType(UUID billId, LocalDate notificationDate, ReminderType type);
}

