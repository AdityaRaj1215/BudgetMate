package com.personalfin.server.reminder.dto;

import com.personalfin.server.reminder.model.ReminderFrequency;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record BillResponse(
        UUID id,
        String name,
        String category,
        BigDecimal amount,
        LocalDate nextDueDate,
        ReminderFrequency frequency,
        boolean active,
        Integer remindDaysBefore,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}

