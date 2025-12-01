package com.personalfin.server.split.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SettlementResponse(
        UUID id,
        UUID groupId,
        String fromMember,
        String toMember,
        java.math.BigDecimal amount,
        boolean settled,
        OffsetDateTime settledAt,
        String notes,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}









