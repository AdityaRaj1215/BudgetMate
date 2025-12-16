-- Create sync metadata table to track last sync timestamps per user
CREATE TABLE sync_metadata (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    last_sync_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    device_id VARCHAR(255), -- Optional: track different devices
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, device_id)
);

CREATE INDEX idx_sync_metadata_user_id ON sync_metadata (user_id);
CREATE INDEX idx_sync_metadata_last_sync ON sync_metadata (last_sync_at);

