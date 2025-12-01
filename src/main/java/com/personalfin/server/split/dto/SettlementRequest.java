package com.personalfin.server.split.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record SettlementRequest(
        @NotNull(message = "Group ID is required")
        UUID groupId,

        @NotBlank(message = "From member is required")
        String fromMember,

        @NotBlank(message = "To member is required")
        String toMember,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than zero")
        BigDecimal amount,

        String notes
) {
}









