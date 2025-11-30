package com.personalfin.server.split.web;

import com.personalfin.server.split.dto.ExpenseGroupRequest;
import com.personalfin.server.split.dto.ExpenseGroupResponse;
import com.personalfin.server.split.dto.SettlementRequest;
import com.personalfin.server.split.dto.SettlementResponse;
import com.personalfin.server.split.dto.SplitExpenseRequest;
import com.personalfin.server.split.model.ExpenseShare;
import com.personalfin.server.split.service.SettlementService;
import com.personalfin.server.split.service.SplitBillService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/split-bills")
public class SplitBillController {

    private final SplitBillService splitBillService;
    private final SettlementService settlementService;

    public SplitBillController(SplitBillService splitBillService, SettlementService settlementService) {
        this.splitBillService = splitBillService;
        this.settlementService = settlementService;
    }

    @PostMapping("/groups")
    public ResponseEntity<ExpenseGroupResponse> createGroup(@Valid @RequestBody ExpenseGroupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(splitBillService.createGroup(request));
    }

    @PutMapping("/groups/{id}")
    public ResponseEntity<ExpenseGroupResponse> updateGroup(
            @PathVariable UUID id,
            @Valid @RequestBody ExpenseGroupRequest request) {
        return ResponseEntity.ok(splitBillService.updateGroup(id, request));
    }

    @DeleteMapping("/groups/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable UUID id) {
        splitBillService.deleteGroup(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/groups")
    public List<ExpenseGroupResponse> listGroups() {
        return splitBillService.listGroups();
    }

    @GetMapping("/groups/{id}")
    public ResponseEntity<ExpenseGroupResponse> getGroup(@PathVariable UUID id) {
        return ResponseEntity.ok(splitBillService.getGroup(id));
    }

    @PostMapping("/split")
    public ResponseEntity<List<ExpenseShare>> splitExpense(@Valid @RequestBody SplitExpenseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(splitBillService.splitExpense(request));
    }

    @PostMapping("/settlements")
    public ResponseEntity<SettlementResponse> createSettlement(@Valid @RequestBody SettlementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(settlementService.createSettlement(request));
    }

    @PostMapping("/settlements/{id}/settle")
    public ResponseEntity<SettlementResponse> markSettled(@PathVariable UUID id) {
        return ResponseEntity.ok(settlementService.markSettled(id));
    }

    @GetMapping("/settlements")
    public List<SettlementResponse> getSettlements(@RequestParam UUID groupId) {
        return settlementService.getSettlements(groupId);
    }

    @GetMapping("/settlements/pending")
    public List<SettlementResponse> getPendingSettlements(@RequestParam UUID groupId) {
        return settlementService.getPendingSettlements(groupId);
    }

    @PostMapping("/settlements/calculate")
    public ResponseEntity<List<SettlementResponse>> calculateSettlements(@RequestParam UUID groupId) {
        return ResponseEntity.ok(settlementService.calculateSettlements(groupId));
    }
}







