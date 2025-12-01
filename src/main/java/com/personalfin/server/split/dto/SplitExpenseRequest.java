package com.personalfin.server.split.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record SplitExpenseRequest(
        @NotNull(message = "Group ID is required")
        UUID groupId,

        UUID expenseId,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than zero")
        BigDecimal amount,

        @NotEmpty(message = "At least one member share is required")
        Map<@NotBlank String, @NotNull @DecimalMin(value = "0.0", inclusive = true) BigDecimal> memberShares
) {
}









