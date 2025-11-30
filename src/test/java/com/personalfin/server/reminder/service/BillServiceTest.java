package com.personalfin.server.reminder.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.personalfin.server.reminder.config.ReminderProperties;
import com.personalfin.server.reminder.dto.BillRequest;
import com.personalfin.server.reminder.dto.BillResponse;
import com.personalfin.server.reminder.dto.MarkBillPaidRequest;
import com.personalfin.server.reminder.exception.BillNotFoundException;
import com.personalfin.server.reminder.model.Bill;
import com.personalfin.server.reminder.model.ReminderFrequency;
import com.personalfin.server.reminder.repository.BillRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BillServiceTest {

    @Mock
    private BillRepository billRepository;

    private BillService billService;

    @BeforeEach
    void setUp() {
        ReminderProperties properties = new ReminderProperties();
        properties.getNotification().setDaysBefore(3);
        billService = new BillService(billRepository, properties);
    }

    @Test
    void shouldCreateBillWithDefaultReminderDays() {
        BillRequest request = new BillRequest(
                "Rent",
                "Housing",
                BigDecimal.valueOf(12000),
                LocalDate.now().plusDays(5),
                ReminderFrequency.MONTHLY,
                null
        );

        Bill persisted = new Bill();
        persisted.setName(request.name());
        persisted.setCategory(request.category());
        persisted.setAmount(request.amount());
        persisted.setNextDueDate(request.nextDueDate());
        persisted.setFrequency(request.frequency());
        when(billRepository.save(any(Bill.class))).thenReturn(persisted);

        BillResponse response = billService.create(request);

        assertThat(response.remindDaysBefore()).isEqualTo(3);
        assertThat(response.frequency()).isEqualTo(ReminderFrequency.MONTHLY);
    }

    @Test
    void shouldMoveNextDueDateWhenMarkedPaid() {
        UUID id = UUID.randomUUID();
        Bill bill = new Bill();
        bill.setName("Internet");
        bill.setNextDueDate(LocalDate.of(2025, 1, 10));
        bill.setFrequency(ReminderFrequency.MONTHLY);
        when(billRepository.findById(id)).thenReturn(Optional.of(bill));

        BillResponse response = billService.markPaid(id, new MarkBillPaidRequest(LocalDate.of(2025, 1, 10)));

        assertThat(response.nextDueDate()).isEqualTo(LocalDate.of(2025, 2, 10));
    }

    @Test
    void shouldThrowWhenBillMissing() {
        UUID id = UUID.randomUUID();
        when(billRepository.findById(id)).thenReturn(Optional.empty());

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                billService.markPaid(id, new MarkBillPaidRequest(LocalDate.now())))
                .isInstanceOf(BillNotFoundException.class);
    }
}

