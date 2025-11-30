package com.personalfin.server.preferences.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserPreferencesResponse(
        UUID id,
        String userId,
        String theme,
        String currency,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}







