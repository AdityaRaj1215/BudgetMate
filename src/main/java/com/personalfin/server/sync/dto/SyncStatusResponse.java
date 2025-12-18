package com.personalfin.server.sync.dto;

import java.time.OffsetDateTime;

public record SyncStatusResponse(
        OffsetDateTime lastSyncAt,
        boolean hasUnsyncedChanges,
        int pendingChangesCount
) {}



