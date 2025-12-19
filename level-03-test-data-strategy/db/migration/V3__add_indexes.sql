-- V3: Additional performance indexes
CREATE INDEX idx_accounts_balance ON accounts(balance) WHERE status = 'ACTIVE';
CREATE INDEX idx_transactions_failed ON transactions(status, timestamp) WHERE status = 'FAILED';

-- Partial index for recent transactions (last 30 days)
CREATE INDEX idx_transactions_recent ON transactions(timestamp DESC) 
    WHERE timestamp > CURRENT_TIMESTAMP - INTERVAL '30 days';
