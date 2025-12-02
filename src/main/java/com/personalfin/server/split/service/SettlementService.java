package com.personalfin.server.split.service;

import com.personalfin.server.split.dto.SettlementRequest;
import com.personalfin.server.split.dto.SettlementResponse;
import com.personalfin.server.split.model.ExpenseGroup;
import com.personalfin.server.split.model.Settlement;
import com.personalfin.server.split.repository.ExpenseGroupRepository;
import com.personalfin.server.split.repository.ExpenseShareRepository;
import com.personalfin.server.split.repository.SettlementRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final ExpenseGroupRepository groupRepository;
    private final ExpenseShareRepository shareRepository;

    public SettlementService(
            SettlementRepository settlementRepository,
            ExpenseGroupRepository groupRepository,
            ExpenseShareRepository shareRepository) {
        this.settlementRepository = settlementRepository;
        this.groupRepository = groupRepository;
        this.shareRepository = shareRepository;
    }

    @Transactional
    public SettlementResponse createSettlement(SettlementRequest request) {
        ExpenseGroup group = groupRepository.findById(request.groupId())
                .orElseThrow(() -> new RuntimeException("Expense group not found: " + request.groupId()));

        Settlement settlement = new Settlement();
        settlement.setGroup(group);
        settlement.setFromMember(request.fromMember());
        settlement.setToMember(request.toMember());
        settlement.setAmount(request.amount());
        settlement.setSettled(false);
        settlement.setNotes(request.notes());

        Settlement saved = settlementRepository.save(settlement);
        return toResponse(saved);
    }

    @Transactional
    public SettlementResponse markSettled(UUID id) {
        Settlement settlement = settlementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Settlement not found: " + id));
        settlement.setSettled(true);
        settlement.setSettledAt(OffsetDateTime.now(ZoneOffset.UTC));
        return toResponse(settlement);
    }

    public List<SettlementResponse> getSettlements(UUID groupId) {
        return settlementRepository.findByGroupId(groupId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<SettlementResponse> getPendingSettlements(UUID groupId) {
        return settlementRepository.findByGroupIdAndSettledFalse(groupId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public List<SettlementResponse> calculateSettlements(UUID groupId) {
        // Calculate who owes whom based on expense shares
        List<ExpenseShareRepository.MemberBalanceProjection> balances =
                shareRepository.getMemberBalances(groupId);

        Map<String, BigDecimal> netBalances = new HashMap<>();
        for (ExpenseShareRepository.MemberBalanceProjection balance : balances) {
            String member = balance.getMemberName();
            BigDecimal total = balance.getTotal();
            netBalances.put(member, netBalances.getOrDefault(member, BigDecimal.ZERO).add(total));
        }

        // Calculate net amounts (what each person should pay/receive)
        // Positive = owes money, Negative = should receive money
        Map<String, BigDecimal> netAmounts = new HashMap<>();
        BigDecimal total = netBalances.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal average = total.divide(BigDecimal.valueOf(netBalances.size()), 2, java.math.RoundingMode.HALF_UP);

        for (Map.Entry<String, BigDecimal> entry : netBalances.entrySet()) {
            BigDecimal net = entry.getValue().subtract(average);
            netAmounts.put(entry.getKey(), net);
        }

        // Generate settlements (simplified algorithm)
        List<SettlementResponse> settlements = new ArrayList<>();
        List<Map.Entry<String, BigDecimal>> debtors = new ArrayList<>();
        List<Map.Entry<String, BigDecimal>> creditors = new ArrayList<>();

        for (Map.Entry<String, BigDecimal> entry : netAmounts.entrySet()) {
            if (entry.getValue().compareTo(BigDecimal.ZERO) > 0) {
                debtors.add(entry);
            } else if (entry.getValue().compareTo(BigDecimal.ZERO) < 0) {
                creditors.add(entry);
            }
        }

        // Match debtors with creditors
        int debtorIdx = 0;
        int creditorIdx = 0;

        while (debtorIdx < debtors.size() && creditorIdx < creditors.size()) {
            Map.Entry<String, BigDecimal> debtor = debtors.get(debtorIdx);
            Map.Entry<String, BigDecimal> creditor = creditors.get(creditorIdx);

            BigDecimal debt = debtor.getValue();
            BigDecimal credit = creditor.getValue().abs();

            BigDecimal settlementAmount = debt.min(credit);

            Settlement settlement = new Settlement();
            settlement.setGroup(groupRepository.findById(groupId).orElseThrow());
            settlement.setFromMember(debtor.getKey());
            settlement.setToMember(creditor.getKey());
            settlement.setAmount(settlementAmount);
            settlement.setSettled(false);

            Settlement saved = settlementRepository.save(settlement);
            settlements.add(toResponse(saved));

            debtor.setValue(debt.subtract(settlementAmount));
            creditor.setValue(credit.subtract(settlementAmount).negate());

            if (debtor.getValue().compareTo(BigDecimal.ZERO) <= 0) {
                debtorIdx++;
            }
            if (creditor.getValue().compareTo(BigDecimal.ZERO) >= 0) {
                creditorIdx++;
            }
        }

        return settlements;
    }

    private SettlementResponse toResponse(Settlement settlement) {
        return new SettlementResponse(
                settlement.getId(),
                settlement.getGroup().getId(),
                settlement.getFromMember(),
                settlement.getToMember(),
                settlement.getAmount(),
                settlement.isSettled(),
                settlement.getSettledAt(),
                settlement.getNotes(),
                settlement.getCreatedAt(),
                settlement.getUpdatedAt()
        );
    }
}










