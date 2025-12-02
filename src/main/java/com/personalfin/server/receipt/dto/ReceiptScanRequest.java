package com.personalfin.server.receipt.dto;

import jakarta.validation.constraints.NotBlank;

public record ReceiptScanRequest(
        @NotBlank(message = "Image data is required")
        String imageData
) {
}










