package com.personalfin.server.split.service;

import com.personalfin.server.expense.model.Expense;
import com.personalfin.server.expense.repository.ExpenseRepository;
import com.personalfin.server.split.dto.ExpenseGroupRequest;
import com.personalfin.server.split.dto.ExpenseGroupResponse;
import com.personalfin.server.split.dto.SplitExpenseRequest;
import com.personalfin.server.split.model.ExpenseGroup;
import com.personalfin.server.split.model.ExpenseShare;
import com.personalfin.server.split.repository.ExpenseGroupRepository;
import com.personalfin.server.split.repository.ExpenseShareRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class SplitBillService {

    private final ExpenseGroupRepository groupRepository;
    private final ExpenseShareRepository shareRepository;
    private final ExpenseRepository expenseRepository;

    public SplitBillService(
            ExpenseGroupRepository groupRepository,
            ExpenseShareRepository shareRepository,
            ExpenseRepository expenseRepository) {
        this.groupRepository = groupRepository;
        this.shareRepository = shareRepository;
        this.expenseRepository = expenseRepository;
    }

    @Transactional
    public ExpenseGroupResponse createGroup(ExpenseGroupRequest request) {
        ExpenseGroup group = new ExpenseGroup();
        group.setName(request.name());
        group.setDescription(request.description());
        group.setCreatedBy(request.createdBy());
        group.setActive(true);

        ExpenseGroup saved = groupRepository.save(group);
        return toResponse(saved);
    }

    @Transactional
    public ExpenseGroupResponse updateGroup(UUID id, ExpenseGroupRequest request) {
        ExpenseGroup group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense group not found: " + id));
        group.setName(request.name());
        group.setDescription(request.description());
        return toResponse(group);
    }

    @Transactional
    public void deleteGroup(UUID id) {
        ExpenseGroup group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense group not found: " + id));
        groupRepository.delete(group);
    }

    @Transactional
    public List<ExpenseShare> splitExpense(SplitExpenseRequest request) {
        ExpenseGroup group = groupRepository.findById(request.groupId())
                .orElseThrow(() -> new RuntimeException("Expense group not found: " + request.groupId()));

        Expense expense = null;
        if (request.expenseId() != null) {
            expense = expenseRepository.findById(request.expenseId())
                    .orElse(null);
        }

        List<ExpenseShare> shares = new ArrayList<>();
        BigDecimal totalShares = request.memberShares().values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Validate that shares sum to total amount (with small tolerance for rounding)
        BigDecimal difference = totalShares.subtract(request.amount()).abs();
        if (difference.compareTo(BigDecimal.valueOf(0.01)) > 0) {
            throw new RuntimeException("Member shares must sum to the total amount");
        }

        for (Map.Entry<String, BigDecimal> entry : request.memberShares().entrySet()) {
            ExpenseShare share = new ExpenseShare();
            share.setGroup(group);
            share.setExpense(expense);
            share.setMemberName(entry.getKey());
            share.setAmount(entry.getValue());
            share.setPaid(false);
            shares.add(shareRepository.save(share));
        }

        return shares;
    }

    public ExpenseGroupResponse getGroup(UUID id) {
        ExpenseGroup group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense group not found: " + id));
        return toResponse(group);
    }

    public List<ExpenseGroupResponse> listGroups() {
        return groupRepository.findByActiveTrueOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void markSharePaid(UUID shareId, boolean paid) {
        ExpenseShare share = shareRepository.findById(shareId)
                .orElseThrow(() -> new RuntimeException("Expense share not found: " + shareId));
        share.setPaid(paid);
    }

    private ExpenseGroupResponse toResponse(ExpenseGroup group) {
        List<ExpenseShare> shares = shareRepository.findByGroupId(group.getId());
        Map<String, ExpenseGroupResponse.MemberBalance> balanceMap = new HashMap<>();

        for (ExpenseShare share : shares) {
            String memberName = share.getMemberName();
            ExpenseGroupResponse.MemberBalance balance = balanceMap.getOrDefault(memberName,
                    new ExpenseGroupResponse.MemberBalance(memberName, BigDecimal.ZERO, BigDecimal.ZERO));

            BigDecimal owed = balance.totalOwed().add(share.getAmount());
            BigDecimal paid = balance.totalPaid();
            if (share.isPaid()) {
                paid = paid.add(share.getAmount());
            }

            balanceMap.put(memberName, new ExpenseGroupResponse.MemberBalance(memberName, owed, paid));
        }

        List<ExpenseGroupResponse.MemberBalance> memberBalances = new ArrayList<>(balanceMap.values());

        return new ExpenseGroupResponse(
                group.getId(),
                group.getName(),
                group.getDescription(),
                group.getCreatedBy(),
                group.isActive(),
                memberBalances,
                group.getCreatedAt(),
                group.getUpdatedAt()
        );
    }
}







