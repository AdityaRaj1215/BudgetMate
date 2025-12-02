-- Add user_id column to all tables for data isolation

ALTER TABLE expenses ADD COLUMN user_id UUID REFERENCES users (id) ON DELETE CASCADE;
ALTER TABLE bills ADD COLUMN user_id UUID REFERENCES users (id) ON DELETE CASCADE;
ALTER TABLE budgets ADD COLUMN user_id UUID REFERENCES users (id) ON DELETE CASCADE;
ALTER TABLE savings_goals ADD COLUMN user_id UUID REFERENCES users (id) ON DELETE CASCADE;
ALTER TABLE investments ADD COLUMN user_id UUID REFERENCES users (id) ON DELETE CASCADE;
ALTER TABLE expense_groups ADD COLUMN user_id UUID REFERENCES users (id) ON DELETE CASCADE;
ALTER TABLE user_preferences DROP CONSTRAINT IF EXISTS user_preferences_user_id_key;
ALTER TABLE user_preferences ALTER COLUMN user_id TYPE UUID USING user_id::uuid;
ALTER TABLE user_preferences ADD CONSTRAINT fk_user_preferences_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;

-- Create indexes for better query performance
CREATE INDEX idx_expenses_user_id ON expenses (user_id);
CREATE INDEX idx_bills_user_id ON bills (user_id);
CREATE INDEX idx_budgets_user_id ON budgets (user_id);
CREATE INDEX idx_savings_goals_user_id ON savings_goals (user_id);
CREATE INDEX idx_investments_user_id ON investments (user_id);
CREATE INDEX idx_expense_groups_user_id ON expense_groups (user_id);






