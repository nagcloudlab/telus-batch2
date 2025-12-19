# Database Seeding Strategy

## Overview
Use Flyway for database schema versioning and test data seeding. This ensures consistent database state across all environments and test runs.

---

## Flyway Setup

### Maven Dependencies

```xml
<dependencies>
    <!-- Flyway for database migrations -->
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-core</artifactId>
    </dependency>
    
    <!-- PostgreSQL Driver -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
    
    <!-- H2 for unit tests -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- Testcontainers for integration tests -->
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>postgresql</artifactId>
        <version>1.19.3</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### Application Configuration

**src/main/resources/application.yml**

```yaml
spring:
  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://localhost:5432/transfer_service}
    username: ${DATABASE_USER:postgres}
    password: ${DATABASE_PASSWORD:postgres}
    driver-class-name: org.postgresql.Driver
    
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
    schemas: public
    validate-on-migrate: true
```

**src/test/resources/application-test.yml**

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    
  flyway:
    enabled: true
    locations: classpath:db/migration, classpath:db/testdata
```

---

## Migration Scripts Structure

```
src/
├── main/
│   └── resources/
│       └── db/
│           └── migration/
│               ├── V1__create_accounts_table.sql
│               ├── V2__create_transactions_table.sql
│               └── V3__add_indexes.sql
└── test/
    └── resources/
        └── db/
            └── testdata/
                ├── V100__seed_test_accounts.sql
                └── V101__seed_test_transactions.sql
```

---

## Schema Migrations

### V1__create_accounts_table.sql

```sql
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
```

### V2__create_transactions_table.sql

```sql
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
```

### V3__add_indexes.sql

```sql
-- V3: Additional performance indexes
CREATE INDEX idx_accounts_balance ON accounts(balance) WHERE status = 'ACTIVE';
CREATE INDEX idx_transactions_failed ON transactions(status, timestamp) WHERE status = 'FAILED';

-- Partial index for recent transactions (last 30 days)
CREATE INDEX idx_transactions_recent ON transactions(timestamp DESC) 
    WHERE timestamp > CURRENT_TIMESTAMP - INTERVAL '30 days';
```

---

## Test Data Seeding

### V100__seed_test_accounts.sql

```sql
-- V100: Seed test accounts for all scenarios

-- Delete existing test data (for re-runs)
DELETE FROM transactions WHERE source_upi LIKE '%@okaxis' OR destination_upi LIKE '%@okaxis';
DELETE FROM accounts WHERE upi_id LIKE '%@okaxis' OR upi_id LIKE '%@paytm' OR upi_id LIKE '%@ybl';

-- TS-1: Normal users for successful transfers
INSERT INTO accounts (upi_id, phone_number, email, balance, daily_used, monthly_used) VALUES
('alice@okaxis', '+919876543210', 'alice@example.com', 10000.00, 0.00, 0.00),
('bob@paytm', '+919876543211', 'bob@example.com', 5000.00, 0.00, 0.00),
('charlie@ybl', '+919876543212', 'charlie@example.com', 15000.00, 0.00, 0.00);

-- TS-2: Poor user with insufficient balance
INSERT INTO accounts (upi_id, phone_number, email, balance, daily_used, monthly_used) VALUES
('poor@okaxis', '+919876543213', 'poor@example.com', 100.00, 0.00, 0.00);

-- TS-7, TS-8: Rich user for limit testing
INSERT INTO accounts (upi_id, phone_number, email, balance, daily_used, monthly_used) VALUES
('rich@oksbi', '+919876543214', 'rich@example.com', 500000.00, 0.00, 0.00);

-- TS-8: User who has exhausted daily limit
INSERT INTO accounts (upi_id, phone_number, email, balance, daily_used, monthly_used) VALUES
('dailylimit@okaxis', '+919876543215', 'dailylimit@example.com', 50000.00, 90000.00, 90000.00);

-- TS-11: User for concurrency testing
INSERT INTO accounts (upi_id, phone_number, email, balance, daily_used, monthly_used) VALUES
('concurrent@okaxis', '+919876543216', 'concurrent@example.com', 1000.00, 0.00, 0.00);

-- Additional test users
INSERT INTO accounts (upi_id, phone_number, email, balance, daily_used, monthly_used) VALUES
('test1@okaxis', '+919876543217', 'test1@example.com', 25000.00, 0.00, 0.00),
('test2@paytm', '+919876543218', 'test2@example.com', 30000.00, 0.00, 0.00),
('test3@ybl', '+919876543219', 'test3@example.com', 20000.00, 0.00, 0.00);
```

### V101__seed_test_transactions.sql

```sql
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
    
-- Some failed transactions
('TXN-20241219-987655', 'alice@okaxis', 'test1@okaxis', 500.00, 0.00, 0.00, 
    'FAILED', 'Failed due to timeout', CURRENT_TIMESTAMP - INTERVAL '1 day'),
('TXN-20241218-876543', 'alice@okaxis', 'bob@paytm', 100.00, 0.00, 0.00, 
    'FAILED', 'Network error', CURRENT_TIMESTAMP - INTERVAL '2 days'),
    
-- Older transactions
('TXN-20241215-765432', 'alice@okaxis', 'charlie@ybl', 2000.00, 5.00, 2005.00, 
    'SUCCESS', 'Shopping', CURRENT_TIMESTAMP - INTERVAL '5 days'),
('TXN-20241210-654321', 'charlie@ybl', 'alice@okaxis', 750.00, 0.00, 750.00, 
    'SUCCESS', 'Dinner bill split', CURRENT_TIMESTAMP - INTERVAL '10 days'),
('TXN-20241205-543210', 'alice@okaxis', 'bob@paytm', 5000.00, 5.00, 5005.00, 
    'SUCCESS', 'Rent payment', CURRENT_TIMESTAMP - INTERVAL '15 days');

-- TS-9: Existing transaction for idempotency testing
INSERT INTO transactions (
    transaction_id, source_upi, destination_upi, amount, fee, total_debited, 
    status, remarks, timestamp
) VALUES
('TXN-DUPLICATE-001', 'test1@okaxis', 'test2@paytm', 1000.00, 0.00, 1000.00, 
    'SUCCESS', 'Idempotency test', CURRENT_TIMESTAMP - INTERVAL '1 hour');

-- Transaction history for bob@paytm
INSERT INTO transactions (
    transaction_id, source_upi, destination_upi, amount, fee, total_debited, 
    status, remarks, timestamp
) VALUES
('TXN-20241220-111111', 'bob@paytm', 'charlie@ybl', 800.00, 0.00, 800.00, 
    'SUCCESS', 'Gift payment', CURRENT_TIMESTAMP - INTERVAL '3 hours'),
('TXN-20241219-222222', 'test1@okaxis', 'bob@paytm', 400.00, 0.00, 400.00, 
    'SUCCESS', 'Payment received', CURRENT_TIMESTAMP - INTERVAL '1 day'),
('TXN-20241218-333333', 'bob@paytm', 'test2@paytm', 1200.00, 5.00, 1205.00, 
    'SUCCESS', 'Service payment', CURRENT_TIMESTAMP - INTERVAL '2 days');
```

---

## Testcontainers Integration

### TestcontainersConfiguration.java

```java
package com.npci.transfer.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {
    
    @Bean
    @ServiceConnection
    public PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:16-alpine"))
            .withDatabaseName("test_transfer_service")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true); // Reuse container across tests
    }
}
```

### Base Integration Test Class

```java
package com.npci.transfer;

import com.npci.transfer.config.TestcontainersConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration(classes = TestcontainersConfiguration.class)
@Testcontainers
public abstract class BaseIntegrationTest {
    
    // Common test setup
    // Flyway will automatically run migrations and seed data
}
```

---

## Data Cleanup Strategy

### After Each Test

```java
@AfterEach
void cleanup() {
    // Clean up test data
    transactionRepository.deleteAll();
    accountRepository.deleteAll();
    
    // Or use SQL
    jdbcTemplate.execute("TRUNCATE transactions CASCADE");
    jdbcTemplate.execute("TRUNCATE accounts CASCADE");
}
```

### Reset Sequences

```sql
-- Reset auto-increment sequences
ALTER SEQUENCE accounts_id_seq RESTART WITH 1;
ALTER SEQUENCE transactions_id_seq RESTART WITH 1;
```

### Test Isolation Annotation

```java
@Transactional
@Rollback // Automatically rollback after test
@Test
void shouldTransferMoney() {
    // Test code
    // Data automatically cleaned up after test
}
```

---

## Data Versioning Best Practices

### Migration Naming Convention

```
V{version}__{description}.sql
  ↓
V1__initial_schema.sql
V2__add_user_table.sql
V3__add_indexes.sql

Test data (version >= 100):
V100__seed_test_accounts.sql
V101__seed_test_transactions.sql
```

### Rollback Scripts (Optional)

```
U{version}__{description}.sql
  ↓
U1__drop_initial_schema.sql
```

### Environment-Specific Data

```yaml
spring:
  flyway:
    locations: 
      - classpath:db/migration  # Always applied
      - classpath:db/dev         # Dev environment only
      - classpath:db/testdata    # Test environment only
```

---

## Performance Optimization

### Batch Inserts

```sql
-- Use COPY for large datasets (PostgreSQL)
COPY accounts (upi_id, phone_number, email, balance) FROM STDIN CSV;
alice@okaxis,+919876543210,alice@example.com,10000.00
bob@paytm,+919876543211,bob@example.com,5000.00
charlie@ybl,+919876543212,charlie@example.com,15000.00
\.
```

### Disable Constraints During Seed

```sql
BEGIN;

-- Disable triggers and constraints
ALTER TABLE transactions DISABLE TRIGGER ALL;
ALTER TABLE accounts DISABLE TRIGGER ALL;

-- Insert data

-- Re-enable
ALTER TABLE accounts ENABLE TRIGGER ALL;
ALTER TABLE transactions ENABLE TRIGGER ALL;

COMMIT;
```

---

## Verification

### Check Migration Status

```bash
# Maven
mvn flyway:info

# Output:
# | Version | Description          | Installed On        | State   |
# |---------|---------------------|---------------------|---------|
# | 1       | create accounts     | 2024-12-20 10:00:00 | Success |
# | 2       | create transactions | 2024-12-20 10:00:01 | Success |
# | 100     | seed test accounts  | 2024-12-20 10:00:02 | Success |
```

### Validate Data

```java
@Test
void verifyTestDataLoaded() {
    // Verify accounts loaded
    assertThat(accountRepository.count()).isEqualTo(10);
    
    // Verify specific test accounts exist
    Optional<Account> alice = accountRepository.findByUpiId("alice@okaxis");
    assertThat(alice).isPresent();
    assertThat(alice.get().getBalance()).isEqualTo(new BigDecimal("10000.00"));
    
    // Verify transactions loaded
    assertThat(transactionRepository.count()).isGreaterThan(0);
}
```

---

## Next Steps

1. ✅ Create schema migrations (V1, V2, V3)
2. ✅ Create test data seeds (V100, V101)
3. ✅ Setup Testcontainers
4. ⏳ Implement data cleanup strategies
5. ⏳ Create data verification tests
6. ⏳ Document data relationships
