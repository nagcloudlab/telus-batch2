-- V101: Seed test transaction history

-- TS-16, TS-17: Transaction history for alice@okaxis
INSERT INTO transactions (
    transaction_id, source_upi, destination_upi, amount, fee, total_debited, 
    status, remarks, timestamp
) VALUES
-- Recent successful transactions
('TXN-20241220-123456', 'alice@okaxis', 'bob@paytm', 500.00, 0.00, 500.00, 
    'SUCCESS', 'Lunch payment', CURRENT_TIMESTAMP - INTERVAL '1 hour'),
('TXN-20241220-123457', 'bob@paytm', 'alice@okaxis', 300.00, 0.00, 300.00, 
    'SUCCESS', 'Refund', CURRENT_TIMESTAMP - INTERVAL '2 hours'),
('TXN-20241219-987654', 'alice@okaxis', 'charlie@ybl', 1500.00, 5.00, 1505.00, 
    'SUCCESS', 'Monthly subscription', CURRENT_TIMESTAMP - INTERVAL '1 day'),
    
-- Some failed transactions (TS-16)
('TXN-20241219-987655', 'alice@okaxis', 'test1@okaxis', 500.00, 0.00, 0.00, 
    'FAILED', 'Failed transaction', CURRENT_TIMESTAMP - INTERVAL '1 day 3 hours'),
('TXN-20241218-876543', 'alice@okaxis', 'bob@paytm', 100.00, 0.00, 0.00, 
    'FAILED', 'Network error', CURRENT_TIMESTAMP - INTERVAL '2 days'),
    
-- Older transactions
('TXN-20241215-765432', 'alice@okaxis', 'charlie@ybl', 2000.00, 5.00, 2005.00, 
    'SUCCESS', 'Shopping', CURRENT_TIMESTAMP - INTERVAL '5 days'),
('TXN-20241210-654321', 'charlie@ybl', 'alice@okaxis', 750.00, 0.00, 750.00, 
    'SUCCESS', 'Dinner bill split', CURRENT_TIMESTAMP - INTERVAL '10 days'),
('TXN-20241205-543210', 'alice@okaxis', 'bob@paytm', 5000.00, 5.00, 5005.00, 
    'SUCCESS', 'Rent payment', CURRENT_TIMESTAMP - INTERVAL '15 days'),
('TXN-20241201-432109', 'test1@okaxis', 'alice@okaxis', 800.00, 0.00, 800.00, 
    'SUCCESS', 'Payment received', CURRENT_TIMESTAMP - INTERVAL '19 days'),
('TXN-20241125-321098', 'alice@okaxis', 'test2@paytm', 1200.00, 5.00, 1205.00, 
    'SUCCESS', 'Utility bill', CURRENT_TIMESTAMP - INTERVAL '25 days');

-- TS-9: Existing transaction for idempotency testing
INSERT INTO transactions (
    transaction_id, source_upi, destination_upi, amount, fee, total_debited, 
    status, remarks, timestamp
) VALUES
('TXN-DUPLICATE-001', 'test1@okaxis', 'test2@paytm', 1000.00, 0.00, 1000.00, 
    'SUCCESS', 'Idempotency test transaction', CURRENT_TIMESTAMP - INTERVAL '1 hour');

-- Transaction history for bob@paytm (TS-17)
INSERT INTO transactions (
    transaction_id, source_upi, destination_upi, amount, fee, total_debited, 
    status, remarks, timestamp
) VALUES
('TXN-20241220-111111', 'bob@paytm', 'charlie@ybl', 800.00, 0.00, 800.00, 
    'SUCCESS', 'Gift payment', CURRENT_TIMESTAMP - INTERVAL '3 hours'),
('TXN-20241219-222222', 'test1@okaxis', 'bob@paytm', 400.00, 0.00, 400.00, 
    'SUCCESS', 'Payment received', CURRENT_TIMESTAMP - INTERVAL '1 day'),
('TXN-20241218-333333', 'bob@paytm', 'test2@paytm', 1200.00, 5.00, 1205.00, 
    'SUCCESS', 'Service payment', CURRENT_TIMESTAMP - INTERVAL '2 days'),
('TXN-20241215-444444', 'test3@ybl', 'bob@paytm', 600.00, 0.00, 600.00, 
    'SUCCESS', 'Transfer', CURRENT_TIMESTAMP - INTERVAL '5 days'),
('TXN-20241210-555555', 'bob@paytm', 'alice@okaxis', 2500.00, 5.00, 2505.00, 
    'SUCCESS', 'Loan repayment', CURRENT_TIMESTAMP - INTERVAL '10 days');

-- Transaction history for charlie@ybl
INSERT INTO transactions (
    transaction_id, source_upi, destination_upi, amount, fee, total_debited, 
    status, remarks, timestamp
) VALUES
('TXN-20241220-666666', 'charlie@ybl', 'test1@okaxis', 950.00, 0.00, 950.00, 
    'SUCCESS', 'Ticket booking', CURRENT_TIMESTAMP - INTERVAL '4 hours'),
('TXN-20241218-777777', 'test2@paytm', 'charlie@ybl', 1500.00, 5.00, 1505.00, 
    'SUCCESS', 'Freelance payment', CURRENT_TIMESTAMP - INTERVAL '2 days'),
('TXN-20241215-888888', 'charlie@ybl', 'test3@ybl', 300.00, 0.00, 300.00, 
    'SUCCESS', 'Snacks', CURRENT_TIMESTAMP - INTERVAL '5 days');

-- TS-15: Transactions for fee calculation testing
INSERT INTO transactions (
    transaction_id, source_upi, destination_upi, amount, fee, total_debited, 
    status, remarks, timestamp
) VALUES
('TXN-20241220-FEE01', 'feecalc@okaxis', 'bob@paytm', 500.00, 0.00, 500.00, 
    'SUCCESS', 'No fee - amount <= 1000', CURRENT_TIMESTAMP - INTERVAL '6 hours'),
('TXN-20241220-FEE02', 'feecalc@okaxis', 'charlie@ybl', 1000.00, 0.00, 1000.00, 
    'SUCCESS', 'No fee - amount = 1000', CURRENT_TIMESTAMP - INTERVAL '7 hours'),
('TXN-20241220-FEE03', 'feecalc@okaxis', 'test1@okaxis', 1001.00, 5.00, 1006.00, 
    'SUCCESS', 'With fee - amount > 1000', CURRENT_TIMESTAMP - INTERVAL '8 hours'),
('TXN-20241220-FEE04', 'feecalc@okaxis', 'test2@paytm', 5000.00, 5.00, 5005.00, 
    'SUCCESS', 'With fee - high amount', CURRENT_TIMESTAMP - INTERVAL '9 hours');
