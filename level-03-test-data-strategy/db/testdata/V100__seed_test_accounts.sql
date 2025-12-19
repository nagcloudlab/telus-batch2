-- V100: Seed test accounts for all scenarios

-- Delete existing test data (for re-runs in test environment)
DELETE FROM transactions WHERE source_upi LIKE '%@okaxis' OR source_upi LIKE '%@paytm' OR source_upi LIKE '%@ybl' 
    OR destination_upi LIKE '%@okaxis' OR destination_upi LIKE '%@paytm' OR destination_upi LIKE '%@ybl';
DELETE FROM accounts WHERE upi_id LIKE '%@okaxis' OR upi_id LIKE '%@paytm' OR upi_id LIKE '%@ybl';

-- TS-1: Normal users for successful transfers
INSERT INTO accounts (upi_id, phone_number, email, balance, daily_used, monthly_used, status) VALUES
('alice@okaxis', '+919876543210', 'alice.test@example.com', 10000.00, 0.00, 0.00, 'ACTIVE'),
('bob@paytm', '+919876543211', 'bob.test@example.com', 5000.00, 0.00, 0.00, 'ACTIVE'),
('charlie@ybl', '+919876543212', 'charlie.test@example.com', 15000.00, 0.00, 0.00, 'ACTIVE');

-- TS-2: Poor user with insufficient balance
INSERT INTO accounts (upi_id, phone_number, email, balance, daily_used, monthly_used, status) VALUES
('poor@okaxis', '+919876543213', 'poor.test@example.com', 100.00, 0.00, 0.00, 'ACTIVE');

-- TS-7, TS-8: Rich user for limit testing
INSERT INTO accounts (upi_id, phone_number, email, balance, daily_used, monthly_used, status) VALUES
('rich@oksbi', '+919876543214', 'rich.test@example.com', 500000.00, 0.00, 0.00, 'ACTIVE');

-- TS-8: User who has exhausted daily limit (90k of 100k used)
INSERT INTO accounts (upi_id, phone_number, email, balance, daily_used, monthly_used, status) VALUES
('dailylimit@okaxis', '+919876543215', 'dailylimit.test@example.com', 50000.00, 90000.00, 90000.00, 'ACTIVE');

-- TS-11: User for concurrency testing (exact balance scenario)
INSERT INTO accounts (upi_id, phone_number, email, balance, daily_used, monthly_used, status) VALUES
('concurrent@okaxis', '+919876543216', 'concurrent.test@example.com', 1000.00, 0.00, 0.00, 'ACTIVE');

-- Additional test users for various scenarios
INSERT INTO accounts (upi_id, phone_number, email, balance, daily_used, monthly_used, status) VALUES
('test1@okaxis', '+919876543217', 'test1@example.com', 25000.00, 0.00, 0.00, 'ACTIVE'),
('test2@paytm', '+919876543218', 'test2@example.com', 30000.00, 0.00, 0.00, 'ACTIVE'),
('test3@ybl', '+919876543219', 'test3@example.com', 20000.00, 0.00, 0.00, 'ACTIVE');

-- User for fee calculation tests (TS-15)
INSERT INTO accounts (upi_id, phone_number, email, balance, daily_used, monthly_used, status) VALUES
('feecalc@okaxis', '+919876543220', 'feecalc.test@example.com', 50000.00, 0.00, 0.00, 'ACTIVE');
