-- V2: Create transactions table
CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    transaction_id VARCHAR(50) NOT NULL UNIQUE,
    source_upi VARCHAR(100) NOT NULL,
    destination_upi VARCHAR(100) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    fee DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    total_debited DECIMAL(15, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    remarks VARCHAR(255),
    error_code VARCHAR(50),
    error_message TEXT,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT check_amount_positive CHECK (amount > 0),
    CONSTRAINT check_fee_non_negative CHECK (fee >= 0),
    CONSTRAINT check_valid_status CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED', 'REVERSED')),
    CONSTRAINT check_different_upis CHECK (source_upi != destination_upi),
    CONSTRAINT fk_source_upi FOREIGN KEY (source_upi) REFERENCES accounts(upi_id),
    CONSTRAINT fk_destination_upi FOREIGN KEY (destination_upi) REFERENCES accounts(upi_id)
);

-- Indexes for performance
CREATE INDEX idx_transactions_txn_id ON transactions(transaction_id);
CREATE INDEX idx_transactions_source_upi ON transactions(source_upi);
CREATE INDEX idx_transactions_dest_upi ON transactions(destination_upi);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_timestamp ON transactions(timestamp DESC);

-- Composite indexes for common queries
CREATE INDEX idx_transactions_source_timestamp ON transactions(source_upi, timestamp DESC);
CREATE INDEX idx_transactions_dest_timestamp ON transactions(destination_upi, timestamp DESC);

-- Comments
COMMENT ON TABLE transactions IS 'UPI money transfer transactions';
COMMENT ON COLUMN transactions.transaction_id IS 'Unique transaction identifier (TXN-YYYYMMDD-XXXXXX)';
COMMENT ON COLUMN transactions.total_debited IS 'Total amount debited from source (amount + fee)';
