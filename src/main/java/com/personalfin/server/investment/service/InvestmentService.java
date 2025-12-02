package com.personalfin.server.investment.service;

import com.personalfin.server.investment.dto.InvestmentRequest;
import com.personalfin.server.investment.dto.InvestmentResponse;
import com.personalfin.server.investment.model.Investment;
import com.personalfin.server.investment.model.InvestmentType;
import com.personalfin.server.investment.repository.InvestmentRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class InvestmentService {

    private final InvestmentRepository investmentRepository;

    public InvestmentService(InvestmentRepository investmentRepository) {
        this.investmentRepository = investmentRepository;
    }

    @Transactional
    public InvestmentResponse create(InvestmentRequest request) {
        Investment investment = new Investment();
        mapRequestToEntity(request, investment);
        Investment saved = investmentRepository.save(investment);
        return toResponse(saved);
    }

    @Transactional
    public InvestmentResponse update(UUID id, InvestmentRequest request) {
        Investment investment = investmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Investment not found: " + id));
        mapRequestToEntity(request, investment);
        return toResponse(investment);
    }

    @Transactional
    public void delete(UUID id) {
        Investment investment = investmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Investment not found: " + id));
        investmentRepository.delete(investment);
    }

    @Transactional
    public InvestmentResponse updateCurrentValue(UUID id, BigDecimal currentValue) {
        Investment investment = investmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Investment not found: " + id));
        investment.setCurrentValue(currentValue);
        return toResponse(investment);
    }

    @Transactional
    public InvestmentResponse deactivate(UUID id) {
        Investment investment = investmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Investment not found: " + id));
        investment.setActive(false);
        return toResponse(investment);
    }

    public List<InvestmentResponse> list() {
        return investmentRepository.findByActiveTrueOrderByStartDateDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<InvestmentResponse> listByType(InvestmentType type) {
        return investmentRepository.findByTypeAndActiveTrue(type)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<InvestmentResponse> listUpcomingMaturities(LocalDate start, LocalDate end) {
        return investmentRepository.findByMaturityDateBetweenAndActiveTrue(start, end)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public InvestmentResponse getById(UUID id) {
        Investment investment = investmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Investment not found: " + id));
        return toResponse(investment);
    }

    private void mapRequestToEntity(InvestmentRequest request, Investment investment) {
        investment.setName(request.name());
        investment.setType(request.type());
        investment.setPrincipalAmount(request.principalAmount());
        investment.setCurrentValue(request.currentValue());
        investment.setInterestRate(request.interestRate());
        investment.setStartDate(request.startDate());
        investment.setMaturityDate(request.maturityDate());
        investment.setNotes(request.notes());
        investment.setActive(true);
    }

    private InvestmentResponse toResponse(Investment investment) {
        BigDecimal currentValue = investment.getCurrentValue() != null
                ? investment.getCurrentValue()
                : investment.getPrincipalAmount();

        BigDecimal gains = currentValue.subtract(investment.getPrincipalAmount());
        BigDecimal gainsPercentage = investment.getPrincipalAmount().compareTo(BigDecimal.ZERO) > 0
                ? gains.divide(investment.getPrincipalAmount(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        return new InvestmentResponse(
                investment.getId(),
                investment.getName(),
                investment.getType(),
                investment.getPrincipalAmount(),
                currentValue,
                investment.getInterestRate(),
                gains,
                gainsPercentage,
                investment.getStartDate(),
                investment.getMaturityDate(),
                investment.isActive(),
                investment.getNotes(),
                investment.getCreatedAt(),
                investment.getUpdatedAt()
        );
    }
}










