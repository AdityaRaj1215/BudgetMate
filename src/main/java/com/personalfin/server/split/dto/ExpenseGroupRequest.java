package com.personalfin.server.split.dto;

import jakarta.validation.constraints.NotBlank;

public record ExpenseGroupRequest(
        @NotBlank(message = "Name is required")
        String name,
        String description,
        String createdBy
) {
}









