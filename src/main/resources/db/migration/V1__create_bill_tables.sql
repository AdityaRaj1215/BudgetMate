CREATE TABLE bills (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    category VARCHAR(60),
    amount NUMERIC(12, 2),
    next_due_date DATE NOT NULL,
    frequency VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    remind_days_before INTEGER NOT NULL DEFAULT 3,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_bills_next_due_date
    ON bills (next_due_date, active);

CREATE TABLE reminder_notifications (
    id UUID PRIMARY KEY,
    bill_id UUID NOT NULL REFERENCES bills (id) ON DELETE CASCADE,
    notification_date DATE NOT NULL,
    notification_type VARCHAR(20) NOT NULL,
    message TEXT,
    channel VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE UNIQUE INDEX uq_reminder_notifications_bill_date_type
    ON reminder_notifications (bill_id, notification_date, notification_type);

