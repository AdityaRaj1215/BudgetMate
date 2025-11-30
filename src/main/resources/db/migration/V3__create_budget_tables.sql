CREATE TABLE budgets (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    amount NUMERIC(14, 2) NOT NULL,
    month_year DATE NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_budgets_month_year
    ON budgets (month_year, active);

CREATE TABLE daily_spend_limits (
    id UUID PRIMARY KEY,
    budget_id UUID NOT NULL REFERENCES budgets (id) ON DELETE CASCADE,
    date DATE NOT NULL,
    daily_limit NUMERIC(14, 2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    UNIQUE(budget_id, date)
);

CREATE INDEX idx_daily_spend_limits_date
    ON daily_spend_limits (date);







