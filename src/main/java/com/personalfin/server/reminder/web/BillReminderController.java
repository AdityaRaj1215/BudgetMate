package com.personalfin.server.reminder.web;

import com.personalfin.server.reminder.dto.BillRequest;
import com.personalfin.server.reminder.dto.BillResponse;
import com.personalfin.server.reminder.dto.MarkBillPaidRequest;
import com.personalfin.server.reminder.service.BillService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reminders/bills")
public class BillReminderController {

    private final BillService billService;

    public BillReminderController(BillService billService) {
        this.billService = billService;
    }

    @PostMapping
    public ResponseEntity<BillResponse> createBill(@Valid @RequestBody BillRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(billService.create(request));
    }

    @GetMapping
    public List<BillResponse> listBills() {
        return billService.list();
    }

    @PutMapping("/{id}")
    public BillResponse updateBill(@PathVariable UUID id, @Valid @RequestBody BillRequest request) {
        return billService.update(id, request);
    }

    @PostMapping("/{id}/mark-paid")
    public BillResponse markBillPaid(@PathVariable UUID id, @Valid @RequestBody MarkBillPaidRequest request) {
        return billService.markPaid(id, request);
    }

    @PostMapping("/{id}/pause")
    public BillResponse pause(@PathVariable UUID id) {
        return billService.pause(id);
    }

    @PostMapping("/{id}/resume")
    public BillResponse resume(@PathVariable UUID id) {
        return billService.resume(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        billService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

