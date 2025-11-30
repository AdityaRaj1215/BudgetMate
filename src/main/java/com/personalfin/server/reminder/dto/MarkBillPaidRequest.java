package com.personalfin.server.reminder.dto;

import jakarta.validation.constraints.FutureOrPresent;
import java.time.LocalDate;

public record MarkBillPaidRequest(
        @FutureOrPresent(message = "Payment date cannot be past")
        LocalDate paymentDate
) {
    public LocalDate paymentDateOrToday() {
        return paymentDate == null ? LocalDate.now() : paymentDate;
    }
}

