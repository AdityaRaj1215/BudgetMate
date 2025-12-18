package com.personalfin.server.sync.web;

import com.personalfin.server.sync.dto.SyncPullResponse;
import com.personalfin.server.sync.dto.SyncPushRequest;
import com.personalfin.server.sync.dto.SyncPushResponse;
import com.personalfin.server.sync.dto.SyncStatusResponse;
import com.personalfin.server.sync.service.SyncService;
import jakarta.validation.Valid;
import java.time.OffsetDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sync")
public class SyncController {

    private final SyncService syncService;

    public SyncController(SyncService syncService) {
        this.syncService = syncService;
    }

    /**
     * Push local changes to the server
     * 
     * @param request Contains all local changes (create, update, delete operations)
     * @return Response with sync results, conflicts, and server sync timestamp
     */
    @PostMapping("/push")
    public ResponseEntity<SyncPushResponse> push(@Valid @RequestBody SyncPushRequest request) {
        SyncPushResponse response = syncService.push(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Pull server changes since last sync
     * 
     * @param lastSyncAt Optional timestamp of last sync (if not provided, uses stored metadata)
     * @param deviceId Optional device identifier for multi-device sync
     * @return Response with all changes since last sync
     */
    @GetMapping("/pull")
    public ResponseEntity<SyncPullResponse> pull(
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
            OffsetDateTime lastSyncAt,
            @RequestParam(required = false) String deviceId) {
        SyncPullResponse response = syncService.pull(lastSyncAt, deviceId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get sync status (last sync time, pending changes count)
     * 
     * @return Sync status information
     */
    @GetMapping("/status")
    public ResponseEntity<SyncStatusResponse> getStatus() {
        SyncStatusResponse response = syncService.getStatus();
        return ResponseEntity.ok(response);
    }
}



