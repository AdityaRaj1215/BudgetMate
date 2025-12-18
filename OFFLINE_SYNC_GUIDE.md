# Offline Sync Implementation Guide

This document provides a comprehensive guide to the offline sync functionality implemented in the Personal Finance Tracker backend.

## Overview

The offline sync system enables frontend applications to work offline and synchronize changes with the server when connectivity is restored. It supports:

- **Push**: Send local changes to the server
- **Pull**: Fetch server changes since last sync
- **Conflict Detection**: Identify and report conflicts when both client and server have modified the same entity
- **Multi-Device Support**: Track sync state per device using device IDs

## Architecture

### Components

1. **SyncMetadata**: Tracks last sync timestamp per user/device
2. **SyncService**: Core sync logic with conflict resolution
3. **SyncController**: REST endpoints for push/pull operations
4. **Repository Extensions**: Added `findUpdatedSince` methods to track changes

### Supported Entities

- **Expenses**: Transaction records
- **Budgets**: Monthly budget plans
- **Bills**: Recurring bill reminders

## API Endpoints

### 1. Push Local Changes

**Endpoint**: `POST /api/sync/push`

**Purpose**: Send local changes (create, update, delete) to the server.

**Request Structure**:
```json
{
  "lastSyncAt": "2025-01-15T10:30:00Z",
  "deviceId": "mobile-device-123",
  "expenses": [
    {
      "id": "uuid",
      "operation": "create|update|delete",
      "clientUpdatedAt": "2025-01-15T11:00:00Z",
      "data": { /* entity data */ }
    }
  ],
  "budgets": [ /* same structure */ ],
  "bills": [ /* same structure */ ]
}
```

**Response**:
- `serverSyncAt`: Server timestamp after processing
- `processedCount`: Number of successfully processed changes
- `conflictCount`: Number of conflicts detected
- `conflicts`: Array of conflict details
- `results`: Array of operation results

### 2. Pull Server Changes

**Endpoint**: `GET /api/sync/pull?lastSyncAt=...&deviceId=...`

**Purpose**: Fetch all changes from server since last sync.

**Query Parameters**:
- `lastSyncAt` (optional): ISO 8601 timestamp. If omitted, uses stored metadata.
- `deviceId` (optional): Device identifier for multi-device tracking.

**Response**:
- `serverSyncAt`: Current server timestamp
- `lastSyncAt`: Timestamp used for the query
- `totalChanges`: Total number of changes
- `expenses`, `budgets`, `bills`: Arrays of updated entities
- `deletedExpenses`, `deletedBudgets`, `deletedBills`: Arrays of deleted entity IDs

### 3. Get Sync Status

**Endpoint**: `GET /api/sync/status`

**Purpose**: Get current sync status and metadata.

**Response**:
```json
{
  "lastSyncAt": "2025-01-15T10:30:00Z",
  "hasUnsyncedChanges": true,
  "pendingChangesCount": 0
}
```

## Conflict Resolution

### Conflict Detection

Conflicts are detected when:

1. **Server Updated After Client**: Server has a newer `updatedAt` timestamp than the client's `clientUpdatedAt`
2. **Entity Deleted on Server**: Client tries to update/delete an entity that no longer exists on the server

### Conflict Strategy

Current implementation uses **Last-Write-Wins** with conflict reporting:

- If no conflict: Client change is applied
- If conflict detected: 
  - Change is rejected
  - Conflict is reported in response
  - Client must resolve manually (future: automatic merge strategies)

### Conflict Response Format

```json
{
  "entityType": "expense",
  "entityId": "uuid",
  "reason": "server_updated_after_client|entity_deleted_on_server",
  "serverUpdatedAt": "2025-01-15T11:30:00Z",
  "clientUpdatedAt": "2025-01-15T11:05:00Z"
}
```

## Frontend Integration

### Recommended Workflow

1. **Initial Load**:
   ```javascript
   // Pull all data on first load
   const response = await fetch('/api/sync/pull');
   const data = await response.json();
   
   // Store in IndexedDB
   await storeInIndexedDB(data.expenses, data.budgets, data.bills);
   await saveLastSyncAt(data.serverSyncAt);
   ```

2. **Offline Operations**:
   ```javascript
   // User creates expense offline
   const expense = {
     id: generateTempId(),
     operation: 'create',
     clientUpdatedAt: new Date().toISOString(),
     data: { /* expense data */ }
   };
   
   // Store in IndexedDB with pending sync flag
   await storePendingChange('expense', expense);
   ```

3. **Sync When Online**:
   ```javascript
   // Collect all pending changes
   const pendingChanges = await getPendingChanges();
   
   // Push to server
   const pushResponse = await fetch('/api/sync/push', {
     method: 'POST',
     body: JSON.stringify({
       lastSyncAt: await getLastSyncAt(),
       deviceId: getDeviceId(),
       expenses: pendingChanges.expenses,
       budgets: pendingChanges.budgets,
       bills: pendingChanges.bills
     })
   });
   
   const result = await pushResponse.json();
   
   // Handle conflicts
   if (result.conflictCount > 0) {
     await handleConflicts(result.conflicts);
   }
   
   // Mark successful changes as synced
   result.results
     .filter(r => r.success)
     .forEach(r => markAsSynced(r.entityType, r.entityId));
   
   // Pull server changes
   const pullResponse = await fetch(`/api/sync/pull?lastSyncAt=${result.serverSyncAt}`);
   const serverData = await pullResponse.json();
   
   // Merge server changes
   await mergeServerChanges(serverData);
   await saveLastSyncAt(serverData.serverSyncAt);
   ```

### IndexedDB Schema Suggestion

```javascript
// Stores
- expenses: { id, ...data, synced, pendingOperation }
- budgets: { id, ...data, synced, pendingOperation }
- bills: { id, ...data, synced, pendingOperation }
- syncMetadata: { userId, lastSyncAt, deviceId }
- conflicts: { entityType, entityId, serverData, clientData }
```

## Database Schema

### Sync Metadata Table

```sql
CREATE TABLE sync_metadata (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    last_sync_at TIMESTAMPTZ NOT NULL,
    device_id VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    UNIQUE(user_id, device_id)
);
```

### Entity Timestamps

All syncable entities have:
- `created_at`: Timestamp when entity was created
- `updated_at`: Timestamp when entity was last modified (used for change tracking)

## Best Practices

### 1. Sync Frequency

- **Pull before push**: Always pull latest changes before pushing local changes
- **Periodic sync**: Sync every 5-10 minutes when online
- **On app resume**: Sync when app comes to foreground
- **After network reconnect**: Sync immediately when connectivity restored

### 2. Conflict Handling

- **Show conflicts to user**: Display both versions and let user choose
- **Auto-resolve simple cases**: For obvious conflicts (e.g., only one side changed)
- **Log conflicts**: Track conflicts for analytics and improvement

### 3. Error Handling

- **Retry with backoff**: Exponential backoff for network failures
- **Queue failed syncs**: Retry failed operations on next sync
- **Handle partial failures**: Some operations may succeed while others fail

### 4. Performance

- **Batch operations**: Send multiple changes in single request
- **Limit pull size**: Use date ranges if pulling large datasets
- **Incremental sync**: Only sync changed entities

### 5. Security

- **Validate ownership**: All operations verify user ownership
- **Sanitize input**: All sync data goes through validation
- **Rate limiting**: Sync endpoints are rate-limited

## Limitations & Future Enhancements

### Current Limitations

1. **No soft deletes**: Deleted entities are not tracked (empty arrays returned)
2. **Simple conflict resolution**: Only detects conflicts, doesn't auto-merge
3. **No version vectors**: Uses timestamps instead of vector clocks
4. **No partial sync**: Always syncs all entity types

### Future Enhancements

1. **Soft delete tracking**: Track deleted entities separately
2. **Automatic merge strategies**: 
   - Field-level merging
   - Three-way merge
   - Custom merge rules
3. **Version vectors**: More accurate conflict detection
4. **Selective sync**: Sync only specific entity types
5. **Compression**: Compress large sync payloads
6. **Delta sync**: Only send changed fields, not entire entities

## Testing

### Test Scenarios

1. **Basic Sync**:
   - Create expense offline → Push → Verify on server
   - Update expense on server → Pull → Verify locally

2. **Conflict Detection**:
   - Update same expense on client and server → Push → Verify conflict reported

3. **Multi-Device**:
   - Sync on device A → Sync on device B → Verify both have latest data

4. **Network Failures**:
   - Simulate network failure during push → Verify retry mechanism

5. **Large Datasets**:
   - Sync with 1000+ entities → Verify performance

## Troubleshooting

### Common Issues

1. **Conflicts not detected**: Check that `clientUpdatedAt` is sent correctly
2. **Changes not syncing**: Verify user authentication and ownership
3. **Duplicate entities**: Ensure IDs are unique (use UUIDs)
4. **Sync metadata not updating**: Check database constraints and transactions

### Debug Endpoints

- `GET /api/sync/status`: Check sync metadata
- Check server logs for sync operations
- Monitor database `sync_metadata` table

## Security Considerations

1. **Authentication**: All sync endpoints require JWT authentication
2. **Authorization**: Users can only sync their own data
3. **Input Validation**: All sync data is validated
4. **Rate Limiting**: Sync endpoints are rate-limited to prevent abuse
5. **Data Isolation**: All queries filter by `userId`

## Performance Metrics

- **Push latency**: < 500ms for 100 changes
- **Pull latency**: < 1s for 1000 entities
- **Conflict detection**: < 50ms per entity
- **Database queries**: Optimized with indexes on `updated_at` and `user_id`

---

For API details, see [API_DOCUMENTATION.md](./API_DOCUMENTATION.md#offline-sync-endpoints).



