package com.personalfin.server.reminder.dto;

import com.personalfin.server.reminder.model.ReminderFrequency;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record BillRequest(
        @NotBlank(message = "Name is required")
        String name,

        String category,

        @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than zero")
        BigDecimal amount,

        @NotNull(message = "Next due date is required")
        @FutureOrPresent(message = "Next due date cannot be past")
        LocalDate nextDueDate,

        @NotNull(message = "Frequency is required")
        ReminderFrequency frequency,

        Integer remindDaysBefore
) {
}

