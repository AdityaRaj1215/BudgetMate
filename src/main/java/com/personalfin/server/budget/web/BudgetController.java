package com.personalfin.server.budget.web;

import com.personalfin.server.budget.dto.BudgetRequest;
import com.personalfin.server.budget.dto.BudgetResponse;
import com.personalfin.server.budget.dto.CoachMessage;
import com.personalfin.server.budget.dto.DailySpendLimitResponse;
import com.personalfin.server.budget.service.BudgetService;
import com.personalfin.server.budget.service.DailySpendCoachService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
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
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;
    private final DailySpendCoachService coachService;

    public BudgetController(BudgetService budgetService, DailySpendCoachService coachService) {
        this.budgetService = budgetService;
        this.coachService = coachService;
    }

    @PostMapping
    public ResponseEntity<BudgetResponse> create(@Valid @RequestBody BudgetRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(budgetService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody BudgetRequest request) {
        return ResponseEntity.ok(budgetService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        budgetService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<BudgetResponse> deactivate(@PathVariable UUID id) {
        return ResponseEntity.ok(budgetService.deactivate(id));
    }

    @GetMapping
    public List<BudgetResponse> list() {
        return budgetService.list();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BudgetResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(budgetService.getById(id));
    }

    @GetMapping("/current")
    public ResponseEntity<BudgetResponse> getCurrent() {
        BudgetResponse current = budgetService.getCurrentBudget();
        if (current == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(current);
    }

    @GetMapping("/daily-limit")
    public ResponseEntity<DailySpendLimitResponse> getDailyLimit(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        DailySpendLimitResponse response = budgetService.getDailyLimit(date);
        // If no budget exists, still return 200 OK with zero values
        return ResponseEntity.ok(response);
    }

    @GetMapping("/coach")
    public ResponseEntity<CoachMessage> getCoachMessage(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        CoachMessage message;
        if (date == null) {
            message = coachService.checkTodaySpending();
        } else {
            message = coachService.checkDailySpending(date);
        }
        
        if (message == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(message);
    }
}








