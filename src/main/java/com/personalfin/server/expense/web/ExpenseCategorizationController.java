package com.personalfin.server.expense.web;

import com.personalfin.server.expense.dto.ExpenseCategorizationRequest;
import com.personalfin.server.expense.dto.ExpenseCategorizationResponse;
import com.personalfin.server.expense.service.ExpenseCategorizer;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseCategorizationController {

    private final ExpenseCategorizer expenseCategorizer;

    public ExpenseCategorizationController(ExpenseCategorizer expenseCategorizer) {
        this.expenseCategorizer = expenseCategorizer;
    }

    @PostMapping("/categorize")
    public ResponseEntity<ExpenseCategorizationResponse> categorizeExpense(
            @Valid @RequestBody ExpenseCategorizationRequest request) {
        return ResponseEntity.ok(expenseCategorizer.categorize(request));
    }
}

