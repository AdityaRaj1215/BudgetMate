CREATE TABLE expense_groups (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    description TEXT,
    created_by VARCHAR(100),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE expense_shares (
    id UUID PRIMARY KEY,
    group_id UUID NOT NULL REFERENCES expense_groups (id) ON DELETE CASCADE,
    expense_id UUID REFERENCES expenses (id) ON DELETE SET NULL,
    member_name VARCHAR(100) NOT NULL,
    amount NUMERIC(14, 2) NOT NULL,
    paid BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_expense_shares_group_id
    ON expense_shares (group_id);

CREATE INDEX idx_expense_shares_member
    ON expense_shares (group_id, member_name);

CREATE TABLE settlements (
    id UUID PRIMARY KEY,
    group_id UUID NOT NULL REFERENCES expense_groups (id) ON DELETE CASCADE,
    from_member VARCHAR(100) NOT NULL,
    to_member VARCHAR(100) NOT NULL,
    amount NUMERIC(14, 2) NOT NULL,
    settled BOOLEAN NOT NULL DEFAULT FALSE,
    settled_at TIMESTAMPTZ,
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_settlements_group_id
    ON settlements (group_id);

CREATE INDEX idx_settlements_from_member
    ON settlements (group_id, from_member, settled);









