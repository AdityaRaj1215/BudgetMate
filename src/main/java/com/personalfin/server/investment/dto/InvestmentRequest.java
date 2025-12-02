package com.personalfin.server.investment.dto;

import com.personalfin.server.investment.model.InvestmentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record InvestmentRequest(
        @NotBlank(message = "Name is required")
        String name,

        @NotNull(message = "Investment type is required")
        InvestmentType type,

        @NotNull(message = "Principal amount is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Principal amount must be greater than zero")
        BigDecimal principalAmount,

        @DecimalMin(value = "0.0", inclusive = true, message = "Current value cannot be negative")
        BigDecimal currentValue,

        @DecimalMin(value = "0.0", inclusive = true, message = "Interest rate cannot be negative")
        BigDecimal interestRate,

        @NotNull(message = "Start date is required")
        LocalDate startDate,

        @FutureOrPresent(message = "Maturity date cannot be in the past")
        LocalDate maturityDate,

        String notes
) {
}










