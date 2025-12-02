package com.personalfin.server.savings.service;

import com.personalfin.server.savings.dto.SavingsGoalRequest;
import com.personalfin.server.savings.dto.SavingsGoalResponse;
import com.personalfin.server.savings.model.SavingsGoal;
import com.personalfin.server.savings.repository.SavingsGoalRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class SavingsGoalService {

    private final SavingsGoalRepository savingsGoalRepository;

    public SavingsGoalService(SavingsGoalRepository savingsGoalRepository) {
        this.savingsGoalRepository = savingsGoalRepository;
    }

    @Transactional
    public SavingsGoalResponse create(SavingsGoalRequest request) {
        SavingsGoal goal = new SavingsGoal();
        goal.setName(request.name());
        goal.setTargetAmount(request.targetAmount());
        goal.setCurrentAmount(BigDecimal.ZERO);
        goal.setTargetDate(request.targetDate());
        goal.setActive(true);

        SavingsGoal saved = savingsGoalRepository.save(goal);
        return toResponse(saved);
    }

    @Transactional
    public SavingsGoalResponse update(UUID id, SavingsGoalRequest request) {
        SavingsGoal goal = savingsGoalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Savings goal not found: " + id));

        goal.setName(request.name());
        goal.setTargetAmount(request.targetAmount());
        goal.setTargetDate(request.targetDate());

        return toResponse(goal);
    }

    @Transactional
    public void delete(UUID id) {
        SavingsGoal goal = savingsGoalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Savings goal not found: " + id));
        savingsGoalRepository.delete(goal);
    }

    @Transactional
    public SavingsGoalResponse addAmount(UUID id, BigDecimal amount) {
        SavingsGoal goal = savingsGoalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Savings goal not found: " + id));

        BigDecimal newAmount = goal.getCurrentAmount().add(amount);
        goal.setCurrentAmount(newAmount);

        // Auto-complete if target reached
        if (newAmount.compareTo(goal.getTargetAmount()) >= 0) {
            goal.setCurrentAmount(goal.getTargetAmount());
        }

        return toResponse(goal);
    }

    @Transactional
    public SavingsGoalResponse withdrawAmount(UUID id, BigDecimal amount) {
        SavingsGoal goal = savingsGoalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Savings goal not found: " + id));

        BigDecimal newAmount = goal.getCurrentAmount().subtract(amount);
        if (newAmount.compareTo(BigDecimal.ZERO) < 0) {
            newAmount = BigDecimal.ZERO;
        }
        goal.setCurrentAmount(newAmount);

        return toResponse(goal);
    }

    @Transactional
    public SavingsGoalResponse setAmount(UUID id, BigDecimal amount) {
        SavingsGoal goal = savingsGoalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Savings goal not found: " + id));

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            amount = BigDecimal.ZERO;
        }
        if (amount.compareTo(goal.getTargetAmount()) > 0) {
            amount = goal.getTargetAmount();
        }

        goal.setCurrentAmount(amount);
        return toResponse(goal);
    }

    @Transactional
    public SavingsGoalResponse deactivate(UUID id) {
        SavingsGoal goal = savingsGoalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Savings goal not found: " + id));
        goal.setActive(false);
        return toResponse(goal);
    }

    public List<SavingsGoalResponse> list() {
        return savingsGoalRepository.findByActiveTrueOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public SavingsGoalResponse getById(UUID id) {
        SavingsGoal goal = savingsGoalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Savings goal not found: " + id));
        return toResponse(goal);
    }

    private SavingsGoalResponse toResponse(SavingsGoal goal) {
        BigDecimal remaining = goal.getTargetAmount().subtract(goal.getCurrentAmount());
        if (remaining.compareTo(BigDecimal.ZERO) < 0) {
            remaining = BigDecimal.ZERO;
        }

        double progressPercentage = goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0
                ? goal.getCurrentAmount()
                        .divide(goal.getTargetAmount(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue()
                : 0.0;

        if (progressPercentage > 100.0) {
            progressPercentage = 100.0;
        }

        return new SavingsGoalResponse(
                goal.getId(),
                goal.getName(),
                goal.getTargetAmount(),
                goal.getCurrentAmount(),
                remaining,
                progressPercentage,
                goal.getTargetDate(),
                goal.isActive(),
                goal.getCreatedAt(),
                goal.getUpdatedAt()
        );
    }
}










