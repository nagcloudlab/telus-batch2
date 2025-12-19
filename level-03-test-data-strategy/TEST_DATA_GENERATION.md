# Test Data Generation Strategy

## Overview
Generate realistic, consistent test data for all testing scenarios without using production data or PII (Personally Identifiable Information).

---

## Test Data Requirements

### From Level 1 Test Scenarios

| Scenario | Data Needed |
|----------|-------------|
| TS-1: Successful Transfer | 2 accounts with balance |
| TS-2: Insufficient Balance | Account with low balance |
| TS-3: Invalid Source UPI | Non-existent source UPI |
| TS-4: Invalid Destination UPI | Non-existent destination UPI |
| TS-5: Invalid UPI Format | Malformed UPI ID |
| TS-6: Below Minimum | Account with any balance |
| TS-7: Exceeds Limit | Account with high balance |
| TS-8: Daily Limit Exceeded | Account with 90% daily limit used |
| TS-9: Idempotency | Existing transaction |
| TS-10: DB Connection Failure | Any valid data |
| TS-11: Concurrent Transfers | Account with exact balance |
| TS-12: Transaction Timeout | Any valid data |
| TS-13: Same Source/Destination | Single account |
| TS-14: Negative Amount | Any valid accounts |
| TS-15: Fee Calculation | Account with >1000 balance |
| TS-16: Transaction Status | Existing transactions |
| TS-17: Transaction History | Multiple transactions |
| TS-18: Authentication Failure | Invalid token |
| TS-19: Rate Limiting | Multiple requests |
| TS-20: Special Characters | Valid accounts |

---

## Data Generation Approaches

### 1. Programmatic Generation (Datafaker)

**Maven Dependency:**
```xml
<dependency>
    <groupId>net.datafaker</groupId>
    <artifactId>datafaker</artifactId>
    <version>2.0.2</version>
    <scope>test</scope>
</dependency>
```

**Test Data Generator Class:**

```java
package com.npci.transfer.testdata;

import net.datafaker.Faker;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

public class TestDataGenerator {
    
    private static final Faker faker = new Faker();
    private static final String[] BANK_CODES = {
        "okaxis", "paytm", "ybl", "oksbi", "okicici", "okhdfcbank"
    };
    
    /**
     * Generate random UPI ID
     */
    public static String generateUpiId() {
        String username = faker.internet().username()
            .replaceAll("[^a-zA-Z0-9]", "")
            .toLowerCase();
        String bankCode = BANK_CODES[faker.random().nextInt(BANK_CODES.length)];
        return username + "@" + bankCode;
    }
    
    /**
     * Generate UPI ID with specific bank
     */
    public static String generateUpiId(String bankCode) {
        String username = faker.internet().username()
            .replaceAll("[^a-zA-Z0-9]", "")
            .toLowerCase();
        return username + "@" + bankCode;
    }
    
    /**
     * Generate random amount within range
     */
    public static BigDecimal generateAmount(double min, double max) {
        double amount = faker.number().randomDouble(2, (long)min, (long)max);
        return BigDecimal.valueOf(amount);
    }
    
    /**
     * Generate transaction ID
     */
    public static String generateTransactionId() {
        LocalDateTime now = LocalDateTime.now();
        String datePart = String.format("%04d%02d%02d", 
            now.getYear(), now.getMonthValue(), now.getDayOfMonth());
        String randomPart = String.format("%06d", 
            faker.number().numberBetween(100000, 999999));
        return "TXN-" + datePart + "-" + randomPart;
    }
    
    /**
     * Generate realistic remarks
     */
    public static String generateRemarks() {
        String[] templates = {
            "Payment for " + faker.commerce().productName(),
            faker.commerce().department() + " payment",
            "Transfer for " + faker.company().buzzword(),
            faker.food().dish() + " bill payment",
            "Monthly " + faker.commerce().productName()
        };
        return templates[faker.random().nextInt(templates.length)];
    }
    
    /**
     * Generate phone number (Indian format)
     */
    public static String generatePhoneNumber() {
        return "+91" + faker.number().digits(10);
    }
    
    /**
     * Generate email
     */
    public static String generateEmail() {
        return faker.internet().emailAddress();
    }
    
    /**
     * Generate account with balance
     */
    public static Account generateAccount(BigDecimal balance) {
        return Account.builder()
            .upiId(generateUpiId())
            .phoneNumber(generatePhoneNumber())
            .email(generateEmail())
            .balance(balance)
            .dailyLimit(new BigDecimal("100000"))
            .dailyUsed(BigDecimal.ZERO)
            .monthlyLimit(new BigDecimal("1000000"))
            .monthlyUsed(BigDecimal.ZERO)
            .createdAt(LocalDateTime.now().minusDays(faker.number().numberBetween(1, 365)))
            .build();
    }
    
    /**
     * Generate multiple accounts
     */
    public static List<Account> generateAccounts(int count) {
        List<Account> accounts = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            BigDecimal balance = generateAmount(1000, 100000);
            accounts.add(generateAccount(balance));
        }
        return accounts;
    }
    
    /**
     * Generate transaction
     */
    public static Transaction generateTransaction(
            String sourceUPI, 
            String destUPI, 
            BigDecimal amount, 
            String status) {
        
        BigDecimal fee = amount.compareTo(new BigDecimal("1000")) > 0 
            ? new BigDecimal("5.00") 
            : BigDecimal.ZERO;
        
        return Transaction.builder()
            .transactionId(generateTransactionId())
            .sourceUPI(sourceUPI)
            .destinationUPI(destUPI)
            .amount(amount)
            .fee(fee)
            .totalDebited(amount.add(fee))
            .status(status)
            .remarks(generateRemarks())
            .timestamp(LocalDateTime.now().minusMinutes(
                faker.number().numberBetween(0, 10000)))
            .build();
    }
}
```

---

### 2. Data Builder Pattern

**Account Builder:**

```java
package com.npci.transfer.testdata;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AccountBuilder {
    
    private String upiId;
    private String phoneNumber;
    private String email;
    private BigDecimal balance = new BigDecimal("10000");
    private BigDecimal dailyLimit = new BigDecimal("100000");
    private BigDecimal dailyUsed = BigDecimal.ZERO;
    private BigDecimal monthlyLimit = new BigDecimal("1000000");
    private BigDecimal monthlyUsed = BigDecimal.ZERO;
    private LocalDateTime createdAt = LocalDateTime.now();
    
    public static AccountBuilder anAccount() {
        return new AccountBuilder();
    }
    
    public AccountBuilder withUpiId(String upiId) {
        this.upiId = upiId;
        return this;
    }
    
    public AccountBuilder withBalance(BigDecimal balance) {
        this.balance = balance;
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
    
    public AccountBuilder withHighBalance() {
        this.balance = new BigDecimal("500000");
        return this;
    }
    
    public AccountBuilder withDailyLimitExhausted() {
        this.dailyUsed = new BigDecimal("90000");
        return this;
    }
    
    public AccountBuilder withMonthlyLimitExhausted() {
        this.monthlyUsed = new BigDecimal("900000");
        return this;
    }
    
    public Account build() {
        if (upiId == null) {
            upiId = TestDataGenerator.generateUpiId();
        }
        if (phoneNumber == null) {
            phoneNumber = TestDataGenerator.generatePhoneNumber();
        }
        if (email == null) {
            email = TestDataGenerator.generateEmail();
        }
        
        return Account.builder()
            .upiId(upiId)
            .phoneNumber(phoneNumber)
            .email(email)
            .balance(balance)
            .dailyLimit(dailyLimit)
            .dailyUsed(dailyUsed)
            .monthlyLimit(monthlyLimit)
            .monthlyUsed(monthlyUsed)
            .createdAt(createdAt)
            .build();
    }
}
```

**Transfer Request Builder:**

```java
package com.npci.transfer.testdata;

import java.math.BigDecimal;

public class TransferRequestBuilder {
    
    private String sourceUPI;
    private String destinationUPI;
    private BigDecimal amount = new BigDecimal("500");
    private String remarks;
    
    public static TransferRequestBuilder aTransferRequest() {
        return new TransferRequestBuilder();
    }
    
    public TransferRequestBuilder from(String sourceUPI) {
        this.sourceUPI = sourceUPI;
        return this;
    }
    
    public TransferRequestBuilder to(String destinationUPI) {
        this.destinationUPI = destinationUPI;
        return this;
    }
    
    public TransferRequestBuilder withAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }
    
    public TransferRequestBuilder withAmount(String amount) {
        this.amount = new BigDecimal(amount);
        return this;
    }
    
    public TransferRequestBuilder withMinimumAmount() {
        this.amount = new BigDecimal("1");
        return this;
    }
    
    public TransferRequestBuilder withMaximumAmount() {
        this.amount = new BigDecimal("100000");
        return this;
    }
    
    public TransferRequestBuilder exceedingLimit() {
        this.amount = new BigDecimal("150000");
        return this;
    }
    
    public TransferRequestBuilder withRemarks(String remarks) {
        this.remarks = remarks;
        return this;
    }
    
    public TransferRequest build() {
        if (sourceUPI == null) {
            sourceUPI = TestDataGenerator.generateUpiId();
        }
        if (destinationUPI == null) {
            destinationUPI = TestDataGenerator.generateUpiId();
        }
        if (remarks == null) {
            remarks = TestDataGenerator.generateRemarks();
        }
        
        return new TransferRequest(sourceUPI, destinationUPI, amount, remarks);
    }
}
```

---

### 3. Scenario-Specific Data Sets

**Test Data Sets Class:**

```java
package com.npci.transfer.testdata;

import java.math.BigDecimal;
import java.util.*;

public class TestDataSets {
    
    // Pre-defined UPI IDs for consistent testing
    public static class UPIs {
        public static final String ALICE = "alice@okaxis";
        public static final String BOB = "bob@paytm";
        public static final String CHARLIE = "charlie@ybl";
        public static final String POOR_USER = "poor@okaxis";
        public static final String RICH_USER = "rich@oksbi";
        public static final String DAILY_LIMIT_USER = "dailylimit@okaxis";
        public static final String INVALID = "invalid-format";
    }
    
    // Standard test accounts
    public static Map<String, Account> getStandardAccounts() {
        Map<String, Account> accounts = new HashMap<>();
        
        // Alice - Normal user with sufficient balance
        accounts.put(UPIs.ALICE, AccountBuilder.anAccount()
            .withUpiId(UPIs.ALICE)
            .withBalance("10000")
            .build());
        
        // Bob - Normal user
        accounts.put(UPIs.BOB, AccountBuilder.anAccount()
            .withUpiId(UPIs.BOB)
            .withBalance("5000")
            .build());
        
        // Charlie - Another normal user
        accounts.put(UPIs.CHARLIE, AccountBuilder.anAccount()
            .withUpiId(UPIs.CHARLIE)
            .withBalance("15000")
            .build());
        
        // Poor User - Insufficient balance
        accounts.put(UPIs.POOR_USER, AccountBuilder.anAccount()
            .withUpiId(UPIs.POOR_USER)
            .withLowBalance()
            .build());
        
        // Rich User - High balance
        accounts.put(UPIs.RICH_USER, AccountBuilder.anAccount()
            .withUpiId(UPIs.RICH_USER)
            .withHighBalance()
            .build());
        
        // Daily Limit User - Almost exhausted daily limit
        accounts.put(UPIs.DAILY_LIMIT_USER, AccountBuilder.anAccount()
            .withUpiId(UPIs.DAILY_LIMIT_USER)
            .withBalance("50000")
            .withDailyLimitExhausted()
            .build());
        
        return accounts;
    }
    
    // Transactions for history testing
    public static List<Transaction> getTransactionHistory(String upiId, int count) {
        List<Transaction> transactions = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            String otherUpi = TestDataGenerator.generateUpiId();
            BigDecimal amount = TestDataGenerator.generateAmount(100, 10000);
            
            Transaction txn = TestDataGenerator.generateTransaction(
                i % 2 == 0 ? upiId : otherUpi,  // Alternate between sent/received
                i % 2 == 0 ? otherUpi : upiId,
                amount,
                i % 10 == 0 ? "FAILED" : "SUCCESS"  // 10% failure rate
            );
            transactions.add(txn);
        }
        
        return transactions;
    }
    
    // Edge case scenarios
    public static class EdgeCases {
        
        // TS-11: Concurrent transfers - exact balance scenario
        public static Account getAccountForConcurrencyTest() {
            return AccountBuilder.anAccount()
                .withUpiId("concurrent@okaxis")
                .withBalance("1000")
                .build();
        }
        
        // TS-15: Fee calculation scenarios
        public static List<TransferRequest> getFeeCalculationScenarios() {
            return Arrays.asList(
                // No fee (≤ 1000)
                TransferRequestBuilder.aTransferRequest()
                    .withAmount("500")
                    .build(),
                TransferRequestBuilder.aTransferRequest()
                    .withAmount("1000")
                    .build(),
                // With fee (> 1000)
                TransferRequestBuilder.aTransferRequest()
                    .withAmount("1001")
                    .build(),
                TransferRequestBuilder.aTransferRequest()
                    .withAmount("5000")
                    .build()
            );
        }
    }
}
```

---

### 4. Usage in Tests

**Example Unit Test:**

```java
@Test
void shouldTransferSuccessfully() {
    // Arrange
    Account alice = AccountBuilder.anAccount()
        .withUpiId("alice@okaxis")
        .withBalance("10000")
        .build();
    
    Account bob = AccountBuilder.anAccount()
        .withUpiId("bob@paytm")
        .withBalance("5000")
        .build();
    
    TransferRequest request = TransferRequestBuilder.aTransferRequest()
        .from(alice.getUpiId())
        .to(bob.getUpiId())
        .withAmount("500")
        .build();
    
    // Act
    TransferResponse response = transferService.initiateTransfer(request);
    
    // Assert
    assertThat(response.getStatus()).isEqualTo("SUCCESS");
    assertThat(response.getAmount()).isEqualTo(new BigDecimal("500"));
}
```

**Example Integration Test:**

```java
@Test
void shouldHandleInsufficientBalance() {
    // Arrange - Use pre-defined data set
    Account poorUser = TestDataSets.getStandardAccounts()
        .get(TestDataSets.UPIs.POOR_USER);
    
    accountRepository.save(poorUser);
    
    TransferRequest request = TransferRequestBuilder.aTransferRequest()
        .from(TestDataSets.UPIs.POOR_USER)
        .to(TestDataSets.UPIs.BOB)
        .withAmount("500")
        .build();
    
    // Act & Assert
    assertThatThrownBy(() -> transferService.initiateTransfer(request))
        .isInstanceOf(InsufficientBalanceException.class)
        .hasMessageContaining("Insufficient balance");
}
```

---

## Best Practices

### DO ✅
- Use builders for complex objects
- Version test data with code
- Generate realistic data with Faker
- Keep test data small and focused
- Use meaningful names (not "test1", "test2")
- Document data relationships
- Clean up after tests

### DON'T ❌
- Use production data
- Include PII in test data
- Hardcode dates (use relative dates)
- Create dependencies between tests
- Share mutable test data across tests
- Commit generated files to Git
- Use random data in assertions

---

## Performance Considerations

### For Unit Tests
- Generate data in memory
- Minimal setup time (<50ms per test)
- No database interaction

### For Integration Tests
- Use Testcontainers with pre-seeded data
- Reuse containers across tests
- Flyway migrations for schema + seed data

### For Performance Tests
- Pre-generate large datasets
- Store in CSV/JSON files
- Load once, use many times
- Realistic distribution of values

---

## Next Steps

1. ✅ Implement TestDataGenerator class
2. ✅ Create builder classes for domain objects
3. ✅ Define standard test data sets
4. ⏳ Create database seed scripts (next section)
5. ⏳ Implement data versioning
6. ⏳ Setup Testcontainers integration
