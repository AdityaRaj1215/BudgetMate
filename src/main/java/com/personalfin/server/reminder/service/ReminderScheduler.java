package com.personalfin.server.reminder.service;

import com.personalfin.server.reminder.config.ReminderProperties;
import com.personalfin.server.reminder.model.Bill;
import com.personalfin.server.reminder.model.ReminderType;
import com.personalfin.server.reminder.model.ReminderNotificationLog;
import com.personalfin.server.reminder.notification.ReminderNotificationGateway;
import com.personalfin.server.reminder.repository.BillRepository;
import com.personalfin.server.reminder.repository.ReminderNotificationRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ReminderScheduler {

    private final BillRepository billRepository;
    private final ReminderNotificationRepository notificationRepository;
    private final ReminderNotificationGateway notificationGateway;
    private final ReminderProperties properties;
    private final Clock clock;

    public ReminderScheduler(BillRepository billRepository,
                             ReminderNotificationRepository notificationRepository,
                             ReminderNotificationGateway notificationGateway,
                             ReminderProperties properties,
                             Clock clock) {
        this.billRepository = billRepository;
        this.notificationRepository = notificationRepository;
        this.notificationGateway = notificationGateway;
        this.properties = properties;
        this.clock = clock;
    }

    @Scheduled(cron = "${reminder.scheduler.cron:0 0 6 * * *}",
            zone = "${reminder.scheduler.zone-id:Asia/Kolkata}")
    @Transactional
    public void processReminders() {
        LocalDate today = LocalDate.now(clock);
        int windowSize = properties.getNotification().getDaysBefore();
        LocalDate windowEnd = today.plusDays(windowSize);

        List<Bill> bills = billRepository.findByActiveTrueAndNextDueDateBetween(today, windowEnd);
        bills.forEach(bill -> evaluateBill(bill, today));
    }

    void evaluateBill(Bill bill, LocalDate referenceDate) {
        LocalDate dueDate = bill.getNextDueDate();
        int remindDays = bill.getRemindDaysBefore();
        LocalDate remindDate = dueDate.minusDays(remindDays);

        if (dueDate.equals(referenceDate)) {
            sendReminder(bill, ReminderType.DUE, "Payment due today");
        } else if (remindDate.equals(referenceDate) && remindDate.isBefore(dueDate)) {
            sendReminder(bill, ReminderType.UPCOMING, "Payment due on " + dueDate);
        }
    }

    private void sendReminder(Bill bill, ReminderType type, String message) {
        LocalDate today = LocalDate.now(clock);
        UUID billId = bill.getId();
        boolean alreadySent = notificationRepository.existsByBill_IdAndNotificationDateAndType(
                billId, today, type);
        if (alreadySent) {
            return;
        }

        notificationGateway.notify(bill, type, message);
        ReminderNotificationLog log = new ReminderNotificationLog(bill, today, type, message, "LOG");
        notificationRepository.save(log);
    }
}

