# Level 3: Quick Start Guide

## 🚀 Get Test Data Strategy Running in 15 Minutes

This guide helps you set up test data generation, database seeding, and Testcontainers quickly.

---

## Prerequisites

```bash
# Check installations
java --version    # Java 17+
mvn --version     # Maven 3.8+
docker --version  # Docker 20+
```

---

## Step 1: Add Dependencies (3 minutes)

### Add to `pom.xml`:

```xml
<dependencies>
    <!-- Datafaker for realistic test data -->
    <dependency>
        <groupId>net.datafaker</groupId>
        <artifactId>datafaker</artifactId>
        <version>2.0.2</version>
        <scope>test</scope>
    </dependency>
    
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
    
    <!-- Testcontainers -->
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>postgresql</artifactId>
        <version>1.19.3</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>1.19.3</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

## Step 2: Create Directory Structure (1 minute)

```bash
mkdir -p src/main/resources/db/migration
mkdir -p src/test/resources/db/testdata
mkdir -p src/test/java/com/npci/transfer/testdata
```

---

## Step 3: Copy Flyway Migrations (2 minutes)

Copy these files to `src/main/resources/db/migration/`:
- `V1__create_accounts_table.sql`
- `V2__create_transactions_table.sql`
- `V3__add_indexes.sql`

Copy these files to `src/test/resources/db/testdata/`:
- `V100__seed_test_accounts.sql`
- `V101__seed_test_transactions.sql`

---

## Step 4: Configure Application (2 minutes)

### `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/transfer_service
    username: postgres
    password: postgres
    
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
```

### `src/test/resources/application-test.yml`:

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

## Step 5: Create Test Data Generator (3 minutes)

Create `src/test/java/com/npci/transfer/testdata/TestDataGenerator.java`:

```java
package com.npci.transfer.testdata;

import net.datafaker.Faker;
import java.math.BigDecimal;

public class TestDataGenerator {
    
    private static final Faker faker = new Faker();
    private static final String[] BANK_CODES = {
        "okaxis", "paytm", "ybl", "oksbi"
    };
    
    public static String generateUpiId() {
        String username = faker.internet().username()
            .replaceAll("[^a-zA-Z0-9]", "")
            .toLowerCase();
        String bankCode = BANK_CODES[faker.random().nextInt(BANK_CODES.length)];
        return username + "@" + bankCode;
    }
    
    public static BigDecimal generateAmount(double min, double max) {
        double amount = faker.number().randomDouble(2, (long)min, (long)max);
        return BigDecimal.valueOf(amount);
    }
    
    public static String generateTransactionId() {
        return "TXN-" + faker.number().digits(8) + "-" + faker.number().digits(6);
    }
}
```

---

## Step 6: Create Account Builder (2 minutes)

Create `src/test/java/com/npci/transfer/testdata/AccountBuilder.java`:

```java
package com.npci.transfer.testdata;

import java.math.BigDecimal;

public class AccountBuilder {
    
    private String upiId;
    private BigDecimal balance = new BigDecimal("10000");
    
    public static AccountBuilder anAccount() {
        return new AccountBuilder();
    }
    
    public AccountBuilder withUpiId(String upiId) {
        this.upiId = upiId;
        return this;
    }
    
    public AccountBuilder withBalance(String balance) {
        this.balance = new BigDecimal(balance);
        return this;
    }
    
    public AccountBuilder withLowBalance() {
        this.balance = new BigDecimal("100");
        return this;
    }
    
    public Account build() {
        if (upiId == null) {
            upiId = TestDataGenerator.generateUpiId();
        }
        return new Account(upiId, balance);
    }
}
```

---

## Step 7: Setup Testcontainers (2 minutes)

Create `src/test/java/com/npci/transfer/config/TestcontainersConfiguration.java`:

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
            .withReuse(true);
    }
}
```

---

## Step 8: Create Base Test Class (1 minute)

Create `src/test/java/com/npci/transfer/BaseIntegrationTest.java`:

```java
package com.npci.transfer;

import com.npci.transfer.config.TestcontainersConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(classes = TestcontainersConfiguration.class)
@Testcontainers
public abstract class BaseIntegrationTest {
    // Flyway will automatically run migrations and seed data
}
```

---

## Step 9: Verify Setup (2 minutes)

### Create a simple test:

```java
@SpringBootTest
@ActiveProfiles("test")
class TestDataSetupTest extends BaseIntegrationTest {
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Test
    void verifyTestDataLoaded() {
        // Verify accounts loaded
        long accountCount = accountRepository.count();
        assertThat(accountCount).isEqualTo(11);
        
        // Verify alice account exists
        Optional<Account> alice = accountRepository.findByUpiId("alice@okaxis");
        assertThat(alice).isPresent();
        assertThat(alice.get().getBalance())
            .isEqualTo(new BigDecimal("10000.00"));
        
        // Verify transactions loaded
        long txnCount = transactionRepository.count();
        assertThat(txnCount).isEqualTo(29);
    }
}
```

### Run test:

```bash
mvn test -Dtest=TestDataSetupTest

# Expected output:
# [INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
# ✅ Test data loaded successfully!
```

---

## Usage Examples

### Example 1: Using Pre-seeded Data

```java
@Test
void shouldTransferBetweenPreseededAccounts() {
    // Arrange - Use pre-seeded accounts from V100
    String sourceUpi = "alice@okaxis";  // Has ₹10,000
    String destUpi = "bob@paytm";       // Has ₹5,000
    
    TransferRequest request = new TransferRequest(
        sourceUpi, destUpi, new BigDecimal("500"), "Test transfer"
    );
    
    // Act
    TransferResponse response = transferService.initiateTransfer(request);
    
    // Assert
    assertThat(response.getStatus()).isEqualTo("SUCCESS");
}
```

### Example 2: Using Builders

```java
@Test
void shouldHandleInsufficientBalance() {
    // Arrange - Use builder to create specific scenario
    Account poorAccount = AccountBuilder.anAccount()
        .withUpiId("testuser@okaxis")
        .withLowBalance()  // ₹100
        .build();
    
    accountRepository.save(poorAccount);
    
    TransferRequest request = new TransferRequest(
        "testuser@okaxis", "bob@paytm", 
        new BigDecimal("500"), "Should fail"
    );
    
    // Act & Assert
    assertThatThrownBy(() -> transferService.initiateTransfer(request))
        .isInstanceOf(InsufficientBalanceException.class);
}
```

### Example 3: Using TestDataGenerator

```java
@Test
void shouldGenerateRealisticTestData() {
    // Generate 10 accounts with random data
    for (int i = 0; i < 10; i++) {
        String upiId = TestDataGenerator.generateUpiId();
        BigDecimal amount = TestDataGenerator.generateAmount(1000, 100000);
        
        Account account = AccountBuilder.anAccount()
            .withUpiId(upiId)
            .withBalance(amount.toString())
            .build();
        
        accountRepository.save(account);
    }
    
    assertThat(accountRepository.count()).isGreaterThanOrEqualTo(21); // 11 + 10
}
```

---

## Common Commands

### Flyway Commands

```bash
# Check migration status
mvn flyway:info

# Run migrations manually
mvn flyway:migrate

# Clean database (drops all objects)
mvn flyway:clean

# Validate migrations
mvn flyway:validate
```

### Test Commands

```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=TestDataSetupTest

# Run tests with Testcontainers
mvn test -Dspring.profiles.active=test

# Skip tests
mvn clean install -DskipTests
```

---

## Troubleshooting

### Issue: Docker not running
```bash
# Start Docker
docker info

# If not running, start Docker Desktop
```

### Issue: Port 5432 already in use
```bash
# Check what's using port
lsof -i :5432

# Stop PostgreSQL if running locally
brew services stop postgresql
# Or
sudo systemctl stop postgresql
```

### Issue: Testcontainers can't pull image
```bash
# Pull image manually
docker pull postgres:16-alpine

# Check Docker disk space
docker system df
```

### Issue: Flyway migrations not running
```bash
# Check Flyway configuration
mvn flyway:info

# Enable Flyway debug logging in application-test.yml
logging:
  level:
    org.flywaydb: DEBUG
```

---

## File Structure Overview

```
src/
├── main/
│   └── resources/
│       ├── application.yml
│       └── db/
│           └── migration/
│               ├── V1__create_accounts_table.sql
│               ├── V2__create_transactions_table.sql
│               └── V3__add_indexes.sql
└── test/
    ├── java/
    │   └── com/npci/transfer/
    │       ├── BaseIntegrationTest.java
    │       ├── config/
    │       │   └── TestcontainersConfiguration.java
    │       └── testdata/
    │           ├── TestDataGenerator.java
    │           ├── AccountBuilder.java
    │           └── TestDataSets.java
    └── resources/
        ├── application-test.yml
        └── db/
            └── testdata/
                ├── V100__seed_test_accounts.sql
                └── V101__seed_test_transactions.sql
```

---

## Success Checklist

- [ ] All dependencies added to pom.xml
- [ ] Flyway migrations created (V1, V2, V3)
- [ ] Test data seeds created (V100, V101)
- [ ] TestDataGenerator class created
- [ ] AccountBuilder class created
- [ ] Testcontainers configuration created
- [ ] BaseIntegrationTest class created
- [ ] Verification test passes
- [ ] 11 accounts seeded
- [ ] 29 transactions seeded

---

## What's Next?

After completing Level 3:

1. ✅ **Test Data Ready**: All scenarios covered
2. ✅ **Database Seeded**: Consistent state for all tests
3. ✅ **Builders Available**: Easy test data creation
4. ✅ **Testcontainers Working**: Isolated test environment
5. ⏳ **Level 4**: Project Setup with Bad Code
6. ⏳ **Level 5**: TDD Introduction

---

## Quick Reference

### Pre-seeded Test Accounts

| UPI ID | Balance | Purpose |
|--------|---------|---------|
| alice@okaxis | ₹10,000 | Normal user |
| bob@paytm | ₹5,000 | Normal user |
| charlie@ybl | ₹15,000 | Normal user |
| poor@okaxis | ₹100 | Insufficient balance |
| rich@oksbi | ₹500,000 | High balance |
| dailylimit@okaxis | ₹50,000 | Daily limit exhausted |
| concurrent@okaxis | ₹1,000 | Concurrency test |

### Builder Usage Patterns

```java
// Simple account
AccountBuilder.anAccount().build();

// Account with specific UPI
AccountBuilder.anAccount()
    .withUpiId("test@okaxis")
    .build();

// Low balance account
AccountBuilder.anAccount()
    .withLowBalance()
    .build();

// High balance account
AccountBuilder.anAccount()
    .withHighBalance()
    .build();
```

---

**Total Setup Time**: ~15 minutes  
**Complexity**: Medium  
**Dependencies**: Java, Maven, Docker

🎉 **You're ready!** Test data strategy is now fully operational.
