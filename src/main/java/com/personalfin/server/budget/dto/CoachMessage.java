package com.personalfin.server.budget.dto;

import java.math.BigDecimal;

public record CoachMessage(
        String message,
        CoachMessageType type,
        BigDecimal overspendAmount,
        BigDecimal dailyLimit,
        BigDecimal spentAmount
) {
    public enum CoachMessageType {
        INFO,
        WARNING,
        CRITICAL
    }
}







