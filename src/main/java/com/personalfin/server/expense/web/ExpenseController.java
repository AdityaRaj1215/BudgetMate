package com.personalfin.server.expense.web;

import com.personalfin.server.expense.dto.CategorySpendingSummary;
import com.personalfin.server.expense.dto.ExpenseCreateRequest;
import com.personalfin.server.expense.dto.ExpenseCreateResponse;
import com.personalfin.server.expense.dto.ExpenseFilterRequest;
import com.personalfin.server.expense.dto.ExpenseHeatmapPoint;
import com.personalfin.server.expense.dto.ExpenseResponse;
import com.personalfin.server.expense.dto.ExpenseUpdateRequest;
import com.personalfin.server.expense.dto.SpendingPattern;
import com.personalfin.server.expense.service.ExpenseAnalyticsService;
import com.personalfin.server.expense.service.ExpenseService;
import com.personalfin.server.expense.service.ExpensePdfExportService;
import com.personalfin.server.expense.service.ExpenseCsvExportService;
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
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final ExpenseAnalyticsService analyticsService;
    private final ExpensePdfExportService pdfExportService;
    private final ExpenseCsvExportService csvExportService;

    public ExpenseController(ExpenseService expenseService,
                             ExpenseAnalyticsService analyticsService,
                             ExpensePdfExportService pdfExportService,
                             ExpenseCsvExportService csvExportService) {
        this.expenseService = expenseService;
        this.analyticsService = analyticsService;
        this.pdfExportService = pdfExportService;
        this.csvExportService = csvExportService;
    }

    @PostMapping
    public ResponseEntity<ExpenseResponse> create(@Valid @RequestBody ExpenseCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(expenseService.createExpense(request));
    }

    @PostMapping("/with-coach")
    public ResponseEntity<ExpenseCreateResponse> createWithCoach(@Valid @RequestBody ExpenseCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(expenseService.createExpenseWithCoach(request));
    }

    @GetMapping
    public List<ExpenseResponse> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) java.math.BigDecimal minAmount,
            @RequestParam(required = false) java.math.BigDecimal maxAmount,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String paymentMethod) {
        
        // If any filter is provided, use filtered search; otherwise return all
        if (startDate != null || endDate != null || category != null || 
            minAmount != null || maxAmount != null || search != null || paymentMethod != null) {
            
            ExpenseFilterRequest filter = new ExpenseFilterRequest(
                    startDate, endDate, category, minAmount, maxAmount, search, paymentMethod);
            return expenseService.listFiltered(filter);
        }
        
        return expenseService.listAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(expenseService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody ExpenseUpdateRequest request) {
        return ResponseEntity.ok(expenseService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        expenseService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/heatmap")
    public List<ExpenseHeatmapPoint> heatmap(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return expenseService.heatmap(start, end);
    }

    @GetMapping("/analytics/categories")
    public List<CategorySpendingSummary> getCategorySpending(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return analyticsService.getCategorySpending(start, end);
    }

    @GetMapping("/analytics/patterns")
    public List<SpendingPattern> getSpendingPatterns(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return analyticsService.detectOverlaps(start, end);
    }

    @GetMapping("/analytics/weekly")
    public List<SpendingPattern> getWeeklyPatterns(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return analyticsService.getWeeklyPatterns(start, end);
    }

    @GetMapping("/analytics/recurring")
    public List<SpendingPattern> getRecurringExpenses(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(required = false) String category) {
        if (category != null) {
            return analyticsService.getRecurringExpenses(start, end, category);
        }
        return analyticsService.detectOverlaps(start, end);
    }

    @GetMapping("/analytics/comparison")
    public java.util.Map<String, Object> getMonthlyComparison(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate month1Start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate month1End,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate month2Start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate month2End) {
        return analyticsService.getMonthlyComparison(month1Start, month1End, month2Start, month2End);
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportPdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        byte[] pdfBytes = pdfExportService.exportExpenses(start, end);

        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=\"transactions.pdf\"")
                .body(pdfBytes);
    }

    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        byte[] csvBytes = csvExportService.exportExpenses(start, end);

        return ResponseEntity.ok()
                .header("Content-Type", "text/csv; charset=UTF-8")
                .header("Content-Disposition", "attachment; filename=\"transactions.csv\"")
                .body(csvBytes);
    }
}

