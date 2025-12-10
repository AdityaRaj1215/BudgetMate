-- OTP codes for registration verification
CREATE TABLE otp_codes (
    id UUID PRIMARY KEY,
    email VARCHAR(150) NOT NULL,
    code_hash VARCHAR(128) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    used_at TIMESTAMPTZ
);

CREATE INDEX idx_otp_codes_email ON otp_codes (email);
CREATE INDEX idx_otp_codes_expires_at ON otp_codes (expires_at);
CREATE INDEX idx_otp_codes_created_at ON otp_codes (created_at);

