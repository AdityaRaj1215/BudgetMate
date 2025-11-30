CREATE TABLE expenses (
    id UUID PRIMARY KEY,
    description TEXT NOT NULL,
    merchant VARCHAR(120),
    category VARCHAR(60),
    amount NUMERIC(14, 2) NOT NULL,
    transaction_date DATE NOT NULL,
    payment_method VARCHAR(40),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_expenses_transaction_date
    ON expenses (transaction_date);

