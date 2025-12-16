package com.personalfin.server.sync.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record SyncPushResponse(
        OffsetDateTime serverSyncAt,
        int processedCount,
        int conflictCount,
        List<SyncConflict> conflicts,
        List<SyncResult> results
) {
    
    public record SyncConflict(
            String entityType, // "expense", "budget", "bill"
            UUID entityId,
            String reason, // "server_updated_after_client", "entity_deleted_on_server"
            OffsetDateTime serverUpdatedAt,
            OffsetDateTime clientUpdatedAt
    ) {}
    
    public record SyncResult(
            String entityType,
            UUID entityId,
            String operation,
            boolean success,
            String message,
            UUID serverId // In case client sent temporary ID
    ) {}
}

