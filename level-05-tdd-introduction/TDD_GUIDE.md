# Test-Driven Development (TDD) Guide

## What is TDD?

Test-Driven Development is a software development approach where you write tests **BEFORE** writing the implementation code.

### Traditional Development
```
1. Write code
2. Write tests
3. Fix bugs
4. Deploy
```

### TDD Approach
```
1. Write test (fails)
2. Write code (test passes)
3. Refactor
4. Deploy with confidence
```

---

## The Red-Green-Refactor Cycle

### 🔴 RED: Write a Failing Test

**Goal**: Write a test that fails because the feature doesn't exist yet.

**Example**:
```java
@Test
void shouldRejectAmountBelowMinimum() {
    // Arrange
    TransferRequest request = new TransferRequest(
        "alice@okaxis",
        "bob@paytm",
        new BigDecimal("0.50"),  // Below ₹1 minimum
        "Test"
    );
    
    // Act & Assert
    assertThatThrownBy(() -> validator.validate(request))
        .isInstanceOf(InvalidAmountException.class)
        .hasMessage("Minimum transfer amount is ₹1");
}
```

**Run test**: ❌ FAILS (validator doesn't exist yet)

**Why this matters**: The test defines what success looks like.

---

### 🟢 GREEN: Make the Test Pass

**Goal**: Write the **minimum** code to make the test pass.

**Example**:
```java
public class TransferValidator {
    
    private static final BigDecimal MIN_AMOUNT = new BigDecimal("1");
    
    public void validate(TransferRequest request) {
        if (request.getAmount().compareTo(MIN_AMOUNT) < 0) {
            throw new InvalidAmountException("Minimum transfer amount is ₹1");
        }
    }
}
```

**Run test**: ✅ PASSES

**Why minimum code?**: Forces you to add only what's needed, avoiding over-engineering.

---

### 🔵 REFACTOR: Improve the Code

**Goal**: Clean up code while keeping tests green.

**Example**:
```java
public class TransferValidator {
    
    private static final BigDecimal MIN_AMOUNT = new BigDecimal("1");
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("100000");
    
    public void validate(TransferRequest request) {
        validateAmount(request.getAmount());
    }
    
    private void validateAmount(BigDecimal amount) {
        if (amount.compareTo(MIN_AMOUNT) < 0) {
            throw new InvalidAmountException(
                String.format("Minimum transfer amount is %s", MIN_AMOUNT)
            );
        }
    }
}
```

**Run test**: ✅ STILL PASSES

**Why refactor?**: Improve code quality without changing behavior.

---

## TDD Benefits

### 1. Better Design

**Without TDD**:
```java
// Tightly coupled, hard to test
public class TransferService {
    public void transfer(String src, String dst, double amt) {
        // Direct database access
        Account srcAccount = em.find(Account.class, src);
        // Complex logic mixed with infrastructure
    }
}
```

**With TDD**:
```java
// Testable, clean interfaces
public class TransferService {
    private final AccountRepository accountRepository;
    private final TransferValidator validator;
    
    public TransferService(AccountRepository repo, TransferValidator validator) {
        this.accountRepository = repo;
        this.validator = validator;
    }
    
    public TransferResponse transfer(TransferRequest request) {
        validator.validate(request);
        // Clean, testable logic
    }
}
```

**TDD forces you to think about dependencies and interfaces.**

---

### 2. Living Documentation

Tests show how to use your code:

```java
@Test
@DisplayName("Should transfer money successfully when balance is sufficient")
void shouldTransferSuccessfully() {
    // Arrange - Shows how to create objects
    Account source = AccountBuilder.anAccount()
        .withUpiId("alice@okaxis")
        .withBalance("10000")
        .build();
    
    TransferRequest request = new TransferRequest(
        "alice@okaxis", "bob@paytm", new BigDecimal("500"), "Lunch"
    );
    
    // Act - Shows how to call the method
    TransferResponse response = service.transfer(request);
    
    // Assert - Shows expected behavior
    assertThat(response.getStatus()).isEqualTo("SUCCESS");
    assertThat(response.getAmount()).isEqualTo(new BigDecimal("500"));
}
```

**Better than comments**: Tests never lie!

---

### 3. Regression Prevention

**Scenario**: You change code and break something.

**Without Tests**:
- Bug discovered in production
- Users affected
- Emergency fix needed
- Reputation damage

**With Tests**:
- Test fails immediately
- Bug never reaches production
- Fix before commit
- Confidence maintained

---

### 4. Refactoring Safety

**Example**: Extract method

**Before**:
```java
public void transfer(TransferRequest request) {
    // 50 lines of code
}
```

**After refactoring**:
```java
public void transfer(TransferRequest request) {
    validateRequest(request);
    Account source = getSourceAccount(request.getSourceUPI());
    Account dest = getDestinationAccount(request.getDestinationUPI());
    validateBalance(source, request.getAmount());
    executeTransfer(source, dest, request.getAmount());
}
```

**Tests ensure behavior didn't change!**

---

## TDD Example: Adding Transaction Limits

### Requirement
Reject transfers that exceed ₹1,00,000 per transaction.

### Step 1: Write Failing Test (RED)

```java
@Test
void shouldRejectAmountExceedingMaximum() {
    // Arrange
    TransferRequest request = new TransferRequest(
        "rich@oksbi",
        "bob@paytm",
        new BigDecimal("150000"),  // Exceeds ₹1,00,000
        "Too much"
    );
    
    // Act & Assert
    assertThatThrownBy(() -> validator.validate(request))
        .isInstanceOf(InvalidAmountException.class)
        .hasMessageContaining("Maximum per-transaction limit is ₹1,00,000");
}
```

**Run test**: ❌ FAILS (no validation for maximum)

---

### Step 2: Make Test Pass (GREEN)

```java
public class TransferValidator {
    
    private static final BigDecimal MIN_AMOUNT = new BigDecimal("1");
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("100000");
    
    public void validate(TransferRequest request) {
        validateAmount(request.getAmount());
    }
    
    private void validateAmount(BigDecimal amount) {
        if (amount.compareTo(MIN_AMOUNT) < 0) {
            throw new InvalidAmountException("Minimum transfer amount is ₹1");
        }
        
        if (amount.compareTo(MAX_AMOUNT) > 0) {
            throw new InvalidAmountException(
                "Maximum per-transaction limit is ₹1,00,000"
            );
        }
    }
}
```

**Run test**: ✅ PASSES

---

### Step 3: Refactor (REFACTOR)

Extract constants and improve error messages:

```java
public class TransferValidator {
    
    private static final BigDecimal MIN_AMOUNT = new BigDecimal("1");
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("100000");
    
    public void validate(TransferRequest request) {
        validateAmount(request.getAmount());
        validateUpiIds(request.getSourceUPI(), request.getDestinationUPI());
    }
    
    private void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new InvalidAmountException("Amount is required");
        }
        
        if (amount.compareTo(MIN_AMOUNT) < 0) {
            throw new InvalidAmountException(
                String.format("Minimum transfer amount is ₹%s", MIN_AMOUNT)
            );
        }
        
        if (amount.compareTo(MAX_AMOUNT) > 0) {
            throw new InvalidAmountException(
                String.format("Maximum per-transaction limit is ₹%s", 
                    formatAmount(MAX_AMOUNT))
            );
        }
    }
    
    private void validateUpiIds(String sourceUPI, String destinationUPI) {
        if (sourceUPI == null || sourceUPI.trim().isEmpty()) {
            throw new InvalidUpiException("Source UPI ID is required");
        }
        
        if (destinationUPI == null || destinationUPI.trim().isEmpty()) {
            throw new InvalidUpiException("Destination UPI ID is required");
        }
        
        if (sourceUPI.equals(destinationUPI)) {
            throw new InvalidUpiException("Cannot transfer to the same account");
        }
    }
    
    private String formatAmount(BigDecimal amount) {
        return String.format("%.0f", amount);
    }
}
```

**Run ALL tests**: ✅ ALL PASS

---

## TDD Best Practices

### 1. Test One Thing at a Time

**Bad**:
```java
@Test
void shouldValidateEverything() {
    // Tests amount, UPI, balance, limits all at once
    // When it fails, you don't know what broke
}
```

**Good**:
```java
@Test
void shouldRejectNullAmount() { /* ... */ }

@Test
void shouldRejectNegativeAmount() { /* ... */ }

@Test
void shouldRejectAmountBelowMinimum() { /* ... */ }

@Test
void shouldRejectAmountExceedingMaximum() { /* ... */ }
```

---

### 2. Use Descriptive Test Names

**Bad**:
```java
@Test
void test1() { /* ... */ }

@Test
void testTransfer() { /* ... */ }
```

**Good**:
```java
@Test
void shouldTransferSuccessfully_WhenBalanceIsSufficient() { /* ... */ }

@Test
void shouldThrowInsufficientBalanceException_WhenBalanceIsLow() { /* ... */ }

@Test
@DisplayName("Should reject transfer when amount exceeds daily limit")
void shouldRejectTransferExceedingDailyLimit() { /* ... */ }
```

---

### 3. Follow AAA Pattern

**Arrange-Act-Assert**:

```java
@Test
void shouldCalculateFeeCorrectly() {
    // Arrange - Setup test data
    BigDecimal amount = new BigDecimal("1500");
    FeeCalculator calculator = new FeeCalculator();
    
    // Act - Execute the code under test
    BigDecimal fee = calculator.calculateFee(amount);
    
    // Assert - Verify the results
    assertThat(fee).isEqualTo(new BigDecimal("5.00"));
}
```

---

### 4. Test Edge Cases

```java
@Test
void shouldHandleMinimumAmount() {
    // Exactly ₹1
}

@Test
void shouldHandleMaximumAmount() {
    // Exactly ₹1,00,000
}

@Test
void shouldHandleAmountJustBelowMinimum() {
    // ₹0.99
}

@Test
void shouldHandleAmountJustAboveMaximum() {
    // ₹1,00,001
}

@Test
void shouldHandleZeroAmount() {
    // ₹0
}

@Test
void shouldHandleNegativeAmount() {
    // -₹100
}
```

---

### 5. Keep Tests Fast

**Bad** (Slow test):
```java
@Test
void shouldTransfer() {
    // Starts entire Spring context
    // Connects to real database
    // Takes 5 seconds
}
```

**Good** (Fast test):
```java
@Test
void shouldTransfer() {
    // Pure unit test
    // Mocked dependencies
    // Takes 10 milliseconds
}
```

**Rule**: Unit tests should run in <100ms each.

---

### 6. Don't Test Framework Code

**Don't Test**:
```java
@Test
void shouldSaveToDatabase() {
    repository.save(account);
    // Don't test Spring Data JPA - it's already tested!
}
```

**Do Test**:
```java
@Test
void shouldCalculateBalance() {
    // Test YOUR business logic
    BigDecimal balance = account.calculateBalance();
    assertThat(balance).isEqualTo(expected);
}
```

---

## Common TDD Mistakes

### Mistake 1: Writing Tests After Code

**Wrong Order**:
```
1. Write all implementation
2. Write all tests
3. Tests pass (no value!)
```

**Correct Order**:
```
1. Write ONE test (fails)
2. Write code to pass
3. Refactor
4. Repeat
```

---

### Mistake 2: Testing Implementation Details

**Bad**:
```java
@Test
void shouldCallRepositorySaveMethod() {
    service.transfer(request);
    verify(repository).save(any()); // Testing HOW it works
}
```

**Good**:
```java
@Test
void shouldTransferMoney() {
    TransferResponse response = service.transfer(request);
    assertThat(response.getStatus()).isEqualTo("SUCCESS"); // Testing WHAT it does
}
```

---

### Mistake 3: Too Many Assertions

**Bad**:
```java
@Test
void shouldDoEverything() {
    // 20 different assertions
    // When it fails, debugging is hard
}
```

**Good**:
```java
@Test
void shouldSetStatusToSuccess() {
    assertThat(response.getStatus()).isEqualTo("SUCCESS");
}

@Test
void shouldDebitCorrectAmount() {
    assertThat(response.getTotalDebited()).isEqualTo(new BigDecimal("505"));
}
```

---

### Mistake 4: Dependent Tests

**Bad**:
```java
@Test
void test1_createAccount() {
    account = service.create("alice@okaxis");
}

@Test
void test2_transferMoney() {
    service.transfer(account, ...); // Depends on test1!
}
```

**Good**:
```java
@Test
void shouldCreateAccount() {
    Account account = service.create("alice@okaxis");
    // Self-contained test
}

@Test
void shouldTransferMoney() {
    Account account = AccountBuilder.anAccount().build();
    // Each test is independent
}
```

---

## TDD Workflow

### Daily Workflow

```
08:00 - Pick a feature from backlog
08:05 - Write first failing test (RED)
08:10 - Make test pass (GREEN)
08:15 - Refactor
08:20 - Commit

08:20 - Write second test (RED)
08:25 - Make test pass (GREEN)
08:30 - Refactor
08:35 - Commit

... repeat ...

17:00 - All features have tests
17:00 - Code is clean
17:00 - Go home with confidence!
```

---

### When to Commit

```
✅ After each GREEN + REFACTOR cycle
✅ When all tests pass
✅ When code is clean

❌ When tests are failing
❌ When code is messy
❌ At end of day (too late!)
```

---

## TDD with Legacy Code

### Problem
Level 4 bad code is untestable!

### Solution
1. Write tests for NEW features (like we're doing)
2. Gradually refactor OLD code to be testable
3. Add tests as you refactor
4. Eventually achieve high coverage

### Process
```
1. Identify area to change
2. Write characterization tests (document current behavior)
3. Refactor to make testable
4. Write proper unit tests
5. Make changes with confidence
```

---

## Measuring TDD Success

### Code Coverage
- **Target**: >80% line coverage
- **Tool**: JaCoCo
- **Command**: `mvn clean test jacoco:report`

### Mutation Testing
- **Target**: >75% mutation coverage
- **Tool**: PITest
- **Purpose**: Test the quality of your tests

### Defect Rate
- **Before TDD**: 10 bugs per 1000 lines
- **After TDD**: 2 bugs per 1000 lines
- **Improvement**: 80% reduction!

---

## Next Steps

1. ✅ Understand TDD cycle
2. ✅ Learn best practices
3. ⏳ Practice with transaction limits feature
4. ⏳ Apply to Level 4 bad code
5. ⏳ Refactor with confidence in Level 6
