package com.personalfin.server.investment.web;

import com.personalfin.server.investment.dto.InvestmentRequest;
import com.personalfin.server.investment.dto.InvestmentResponse;
import com.personalfin.server.investment.model.InvestmentType;
import com.personalfin.server.investment.service.InvestmentService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
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
@RequestMapping("/api/investments")
public class InvestmentController {

    private final InvestmentService investmentService;

    public InvestmentController(InvestmentService investmentService) {
        this.investmentService = investmentService;
    }

    @PostMapping
    public ResponseEntity<InvestmentResponse> create(@Valid @RequestBody InvestmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(investmentService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InvestmentResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody InvestmentRequest request) {
        return ResponseEntity.ok(investmentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        investmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<InvestmentResponse> deactivate(@PathVariable UUID id) {
        return ResponseEntity.ok(investmentService.deactivate(id));
    }

    @PutMapping("/{id}/current-value")
    public ResponseEntity<InvestmentResponse> updateCurrentValue(
            @PathVariable UUID id,
            @RequestParam BigDecimal currentValue) {
        return ResponseEntity.ok(investmentService.updateCurrentValue(id, currentValue));
    }

    @GetMapping
    public List<InvestmentResponse> list(@RequestParam(required = false) InvestmentType type) {
        if (type != null) {
            return investmentService.listByType(type);
        }
        return investmentService.list();
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvestmentResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(investmentService.getById(id));
    }

    @GetMapping("/maturities")
    public List<InvestmentResponse> getUpcomingMaturities(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return investmentService.listUpcomingMaturities(start, end);
    }
}










