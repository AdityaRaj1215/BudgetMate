package com.personalfin.server.reminder.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.personalfin.server.reminder.config.ReminderProperties;
import com.personalfin.server.reminder.model.Bill;
import com.personalfin.server.reminder.model.ReminderFrequency;
import com.personalfin.server.reminder.model.ReminderType;
import com.personalfin.server.reminder.notification.ReminderNotificationGateway;
import com.personalfin.server.reminder.repository.BillRepository;
import com.personalfin.server.reminder.repository.ReminderNotificationRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReminderSchedulerTest {

    @Mock
    private BillRepository billRepository;

    @Mock
    private ReminderNotificationRepository notificationRepository;

    @Mock
    private ReminderNotificationGateway notificationGateway;

    private ReminderScheduler scheduler;
    private Clock clock;

    @BeforeEach
    void init() {
        ReminderProperties properties = new ReminderProperties();
        properties.getNotification().setDaysBefore(3);
        clock = Clock.fixed(
                LocalDate.of(2025, 1, 10).atStartOfDay(ZoneOffset.UTC).toInstant(),
                ZoneId.of("UTC"));
        scheduler = new ReminderScheduler(
                billRepository,
                notificationRepository,
                notificationGateway,
                properties,
                clock);
    }

    @Test
    void shouldSendUpcomingReminderOnConfiguredDay() {
        Bill bill = new Bill();
        bill.setId(UUID.randomUUID());
        bill.setName("Rent");
        bill.setFrequency(ReminderFrequency.MONTHLY);
        bill.setNextDueDate(LocalDate.of(2025, 1, 13));
        bill.setRemindDaysBefore(3);

        when(billRepository.findByActiveTrueAndNextDueDateBetween(any(), any()))
                .thenReturn(List.of(bill));
        when(notificationRepository.existsByBill_IdAndNotificationDateAndType(any(), any(), any()))
                .thenReturn(false);

        scheduler.processReminders();

        verify(notificationGateway).notify(bill, ReminderType.UPCOMING, "Payment due on 2025-01-13");
    }

    @Test
    void shouldSkipDuplicateReminder() {
        Bill bill = new Bill();
        bill.setId(UUID.randomUUID());
        bill.setName("Internet");
        bill.setFrequency(ReminderFrequency.MONTHLY);
        bill.setNextDueDate(LocalDate.of(2025, 1, 10));
        bill.setRemindDaysBefore(3);

        when(billRepository.findByActiveTrueAndNextDueDateBetween(any(), any()))
                .thenReturn(List.of(bill));
        when(notificationRepository.existsByBill_IdAndNotificationDateAndType(any(), any(), any()))
                .thenReturn(true);

        scheduler.processReminders();

        verify(notificationGateway, never()).notify(any(), any(), any());
    }
}

