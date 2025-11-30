package com.personalfin.server.reminder.service;

import com.personalfin.server.reminder.config.ReminderProperties;
import com.personalfin.server.reminder.dto.BillRequest;
import com.personalfin.server.reminder.dto.BillResponse;
import com.personalfin.server.reminder.dto.MarkBillPaidRequest;
import com.personalfin.server.reminder.exception.BillNotFoundException;
import com.personalfin.server.reminder.model.Bill;
import com.personalfin.server.reminder.model.ReminderFrequency;
import com.personalfin.server.reminder.repository.BillRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class BillService {

    private final BillRepository billRepository;
    private final ReminderProperties properties;

    public BillService(
            BillRepository billRepository,
            ReminderProperties properties) {
        this.billRepository = billRepository;
        this.properties = properties;
    }

    @Transactional
    public BillResponse create(BillRequest request) {
        Bill bill = new Bill();
        mapRequestToEntity(request, bill);
        Bill saved = billRepository.save(bill);
        return toResponse(saved);
    }

    @Transactional
    public BillResponse update(UUID id, BillRequest request) {
        Bill bill = getBill(id);
        mapRequestToEntity(request, bill);
        return toResponse(bill);
    }

    @Transactional
    public void delete(UUID id) {
        Bill bill = getBill(id);
        billRepository.delete(bill);
    }

    @Transactional
    public BillResponse markPaid(UUID id, MarkBillPaidRequest request) {
        Bill bill = getBill(id);
        LocalDate nextDueDate = calculateNextDueDate(bill.getFrequency(), bill.getNextDueDate());
        bill.setNextDueDate(nextDueDate);
        if (bill.getFrequency() == ReminderFrequency.ONE_TIME) {
            bill.setActive(false);
        }
        return toResponse(bill);
    }

    @Transactional
    public BillResponse pause(UUID id) {
        Bill bill = getBill(id);
        bill.setActive(false);
        return toResponse(bill);
    }

    @Transactional
    public BillResponse resume(UUID id) {
        Bill bill = getBill(id);
        bill.setActive(true);
        return toResponse(bill);
    }

    @Transactional
    public BillResponse updateNextDueDate(UUID id, LocalDate nextDueDate) {
        Bill bill = getBill(id);
        bill.setNextDueDate(nextDueDate);
        return toResponse(bill);
    }

    @Transactional
    public List<BillResponse> list() {
        return billRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<Bill> findBillsBetween(LocalDate start, LocalDate end) {
        return billRepository.findByActiveTrueAndNextDueDateBetween(start, end);
    }

    private void mapRequestToEntity(BillRequest request, Bill bill) {
        bill.setName(request.name());
        bill.setCategory(request.category());
        bill.setAmount(request.amount());
        bill.setNextDueDate(request.nextDueDate());
        bill.setFrequency(request.frequency());
        bill.setRemindDaysBefore(resolveRemindDays(request.remindDaysBefore()));
        bill.setActive(true);
    }

    private int resolveRemindDays(Integer customValue) {
        if (customValue == null || customValue < 0) {
            return properties.getNotification().getDaysBefore();
        }
        return customValue;
    }

    private Bill getBill(UUID id) {
        return billRepository.findById(id)
                .orElseThrow(() -> new BillNotFoundException(id));
    }

    private BillResponse toResponse(Bill bill) {
        return new BillResponse(
                bill.getId(),
                bill.getName(),
                bill.getCategory(),
                bill.getAmount(),
                bill.getNextDueDate(),
                bill.getFrequency(),
                bill.isActive(),
                bill.getRemindDaysBefore(),
                bill.getCreatedAt(),
                bill.getUpdatedAt()
        );
    }

    private LocalDate calculateNextDueDate(ReminderFrequency frequency, LocalDate currentDueDate) {
        return switch (frequency) {
            case MONTHLY -> currentDueDate.plusMonths(1);
            case QUARTERLY -> currentDueDate.plusMonths(3);
            case YEARLY -> currentDueDate.plusYears(1);
            case WEEKLY -> currentDueDate.plusWeeks(1);
            case ONE_TIME -> currentDueDate;
        };
    }
}

