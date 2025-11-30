CREATE TABLE investments (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    type VARCHAR(20) NOT NULL,
    principal_amount NUMERIC(14, 2) NOT NULL,
    current_value NUMERIC(14, 2),
    interest_rate NUMERIC(5, 2),
    start_date DATE NOT NULL,
    maturity_date DATE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_investments_type
    ON investments (type, active);

CREATE INDEX idx_investments_maturity_date
    ON investments (maturity_date, active);







