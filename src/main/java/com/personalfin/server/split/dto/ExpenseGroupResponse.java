package com.personalfin.server.split.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record ExpenseGroupResponse(
        UUID id,
        String name,
        String description,
        String createdBy,
        boolean active,
        List<MemberBalance> memberBalances,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public record MemberBalance(
            String memberName,
            java.math.BigDecimal totalOwed,
            java.math.BigDecimal totalPaid
    ) {
    }
}










