-- V1: Create accounts table
CREATE TABLE accounts (
    id BIGSERIAL PRIMARY KEY,
    upi_id VARCHAR(100) NOT NULL UNIQUE,
    phone_number VARCHAR(20) NOT NULL,
    email VARCHAR(255) NOT NULL,
    balance DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    daily_limit DECIMAL(15, 2) NOT NULL DEFAULT 100000.00,
    daily_used DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    monthly_limit DECIMAL(15, 2) NOT NULL DEFAULT 1000000.00,
    monthly_used DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT check_balance_positive CHECK (balance >= 0),
    CONSTRAINT check_daily_used_valid CHECK (daily_used >= 0 AND daily_used <= daily_limit),
    CONSTRAINT check_monthly_used_valid CHECK (monthly_used >= 0 AND monthly_used <= monthly_limit),
    CONSTRAINT check_upi_format CHECK (upi_id ~ '^[a-zA-Z0-9.\-_]+@[a-zA-Z]+$')
);

-- Indexes for performance
CREATE INDEX idx_accounts_upi_id ON accounts(upi_id);
CREATE INDEX idx_accounts_phone ON accounts(phone_number);
CREATE INDEX idx_accounts_status ON accounts(status);

-- Comments for documentation
COMMENT ON TABLE accounts IS 'UPI accounts for money transfers';
COMMENT ON COLUMN accounts.upi_id IS 'Unique UPI identifier (username@bankcode)';
COMMENT ON COLUMN accounts.daily_limit IS 'Maximum transfer limit per day';
COMMENT ON COLUMN accounts.daily_used IS 'Amount already transferred today';
