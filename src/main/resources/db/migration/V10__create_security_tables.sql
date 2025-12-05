-- Login attempts table for tracking authentication attempts
CREATE TABLE login_attempts (
    id UUID PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    ip_address VARCHAR(45),
    successful BOOLEAN NOT NULL,
    failure_reason VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_login_attempts_username ON login_attempts (username);
CREATE INDEX idx_login_attempts_ip_address ON login_attempts (ip_address);
CREATE INDEX idx_login_attempts_created_at ON login_attempts (created_at);

-- Audit logs table for security event logging
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL,
    user_id UUID,
    username VARCHAR(100),
    ip_address VARCHAR(45),
    details TEXT,
    success BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_audit_logs_user_id ON audit_logs (user_id);
CREATE INDEX idx_audit_logs_event_type ON audit_logs (event_type);
CREATE INDEX idx_audit_logs_created_at ON audit_logs (created_at);

