CREATE TABLE user_preferences (
    id UUID PRIMARY KEY,
    user_id VARCHAR(100) UNIQUE,
    theme VARCHAR(20) DEFAULT 'light',
    currency VARCHAR(10) DEFAULT 'INR',
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_user_preferences_user_id
    ON user_preferences (user_id);









