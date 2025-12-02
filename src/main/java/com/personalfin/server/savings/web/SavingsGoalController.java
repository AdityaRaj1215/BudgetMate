package com.personalfin.server.savings.web;

import com.personalfin.server.savings.dto.SavingsGoalRequest;
import com.personalfin.server.savings.dto.SavingsGoalResponse;
import com.personalfin.server.savings.service.SavingsGoalService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
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
@RequestMapping("/api/savings-goals")
public class SavingsGoalController {

    private final SavingsGoalService savingsGoalService;

    public SavingsGoalController(SavingsGoalService savingsGoalService) {
        this.savingsGoalService = savingsGoalService;
    }

    @PostMapping
    public ResponseEntity<SavingsGoalResponse> create(@Valid @RequestBody SavingsGoalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savingsGoalService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SavingsGoalResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody SavingsGoalRequest request) {
        return ResponseEntity.ok(savingsGoalService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        savingsGoalService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<SavingsGoalResponse> deactivate(@PathVariable UUID id) {
        return ResponseEntity.ok(savingsGoalService.deactivate(id));
    }

    @PostMapping("/{id}/add")
    public ResponseEntity<SavingsGoalResponse> addAmount(
            @PathVariable UUID id,
            @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(savingsGoalService.addAmount(id, amount));
    }

    @PostMapping("/{id}/withdraw")
    public ResponseEntity<SavingsGoalResponse> withdrawAmount(
            @PathVariable UUID id,
            @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(savingsGoalService.withdrawAmount(id, amount));
    }

    @PutMapping("/{id}/amount")
    public ResponseEntity<SavingsGoalResponse> setAmount(
            @PathVariable UUID id,
            @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(savingsGoalService.setAmount(id, amount));
    }

    @GetMapping
    public List<SavingsGoalResponse> list() {
        return savingsGoalService.list();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SavingsGoalResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(savingsGoalService.getById(id));
    }
}










