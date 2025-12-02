package com.personalfin.server.investment.dto;

import com.personalfin.server.investment.model.InvestmentType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record InvestmentResponse(
        UUID id,
        String name,
        InvestmentType type,
        BigDecimal principalAmount,
        BigDecimal currentValue,
        BigDecimal interestRate,
        BigDecimal gains,
        BigDecimal gainsPercentage,
        LocalDate startDate,
        LocalDate maturityDate,
        boolean active,
        String notes,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}










