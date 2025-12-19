# Comprehensive Unit Testing Guide

## Overview

Unit testing is the foundation of software quality. This guide covers everything you need to write effective, maintainable unit tests.

---

## Testing Principles

### 1. F.I.R.S.T Principles

**F - Fast**: Tests should run in milliseconds
- Unit tests: <100ms each
- Full suite: <10 seconds
- No database, no network, no file I/O

**I - Independent**: Tests should not depend on each other
- Can run in any order
- No shared state
- Each test sets up its own data

**R - Repeatable**: Same result every time
- No flaky tests
- Deterministic
- No random data (unless seeded)

**S - Self-Validating**: Pass or fail, no manual checking
- Automated assertions
- Clear failure messages
- No log inspection needed

**T - Timely**: Written at the right time
- TDD: Written before code
- At latest: Written with code
- Never: After deployment

---

### 2. Test Naming Conventions

**Bad Names**:
```java
@Test
void test1() { }

@Test
void testTransfer() { }

@Test
void transferTest() { }
```

**Good Names**:
```java
@Test
void shouldTransferSuccessfully_WhenBalanceIsSufficient() { }

@Test
void shouldThrowInsufficientBalanceException_WhenBalanceTooLow() { }

@Test
@DisplayName("Should calculate fee correctly for amounts over ₹1,000")
void shouldCalculateFeeCorrectly() { }
```

**Naming Pattern**: `should[ExpectedBehavior]_When[StateUnderTest]`

---

### 3. AAA Pattern (Arrange-Act-Assert)

```java
@Test
void shouldTransferSuccessfully() {
    // Arrange - Set up test data and dependencies
    Account source = AccountBuilder.anAccount()
        .withBalance("10000")
        .build();
    TransferRequest request = new TransferRequest(...);
    
    // Act - Execute the code under test
    TransferResponse response = service.transfer(request);
    
    // Assert - Verify the results
    assertThat(response.getStatus()).isEqualTo("SUCCESS");
    assertThat(source.getBalance()).isEqualTo(new BigDecimal("9500"));
}
```

**Benefits**:
- Clear structure
- Easy to understand
- Easy to maintain

---

## Testing Each Layer

### Controller Layer Testing

**What to Test**:
- HTTP status codes
- Request/response mapping
- Validation
- Error handling

**Don't Test**:
- Business logic (that's in Service)
- Database access (that's in Repository)

**Tools**: MockMvc, @WebMvcTest

**Example**:
```java
@WebMvcTest(TransferController.class)
class TransferControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private TransferService transferService;
    
    @Test
    void shouldReturn200_WhenTransferSuccessful() throws Exception {
        // Arrange
        TransferResponse response = TransferResponse.builder()
            .status("SUCCESS")
            .amount(new BigDecimal("500"))
            .build();
        
        when(transferService.initiateTransfer(any()))
            .thenReturn(response);
        
        // Act & Assert
        mockMvc.perform(post("/v1/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "sourceUPI": "alice@okaxis",
                        "destinationUPI": "bob@paytm",
                        "amount": 500,
                        "remarks": "Test"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SUCCESS"))
            .andExpect(jsonPath("$.amount").value(500));
    }
    
    @Test
    void shouldReturn400_WhenRequestInvalid() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/v1/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "sourceUPI": "",
                        "destinationUPI": "bob@paytm",
                        "amount": -100
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Validation Error"));
    }
}
```

---

### Service Layer Testing

**What to Test**:
- Business logic
- Validation rules
- Calculations
- State changes
- Exception handling

**Tools**: @ExtendWith(MockitoExtension.class), @Mock, @InjectMocks

**Example**:
```java
@ExtendWith(MockitoExtension.class)
class TransferServiceTest {
    
    @Mock
    private AccountRepository accountRepository;
    
    @Mock
    private TransactionRepository transactionRepository;
    
    @Mock
    private FeeCalculator feeCalculator;
    
    @InjectMocks
    private TransferService transferService;
    
    @Test
    void shouldTransferSuccessfully() {
        // Arrange
        Account source = new Account();
        source.setBalance(new BigDecimal("10000"));
        
        Account dest = new Account();
        dest.setBalance(new BigDecimal("5000"));
        
        when(accountRepository.findByUpiId("alice@okaxis"))
            .thenReturn(Optional.of(source));
        when(accountRepository.findByUpiId("bob@paytm"))
            .thenReturn(Optional.of(dest));
        when(feeCalculator.calculateFee(any()))
            .thenReturn(BigDecimal.ZERO);
        
        TransferRequest request = new TransferRequest(
            "alice@okaxis", "bob@paytm", new BigDecimal("500"), "Test"
        );
        
        // Act
        TransferResponse response = transferService.initiateTransfer(request);
        
        // Assert
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        assertThat(source.getBalance()).isEqualTo(new BigDecimal("9500"));
        assertThat(dest.getBalance()).isEqualTo(new BigDecimal("5500"));
        
        verify(accountRepository, times(2)).save(any());
        verify(transactionRepository, times(1)).save(any());
    }
}
```

---

### Repository Layer Testing

**What to Test**:
- Query correctness
- Data persistence
- Custom queries
- Transaction handling

**Tools**: @DataJpaTest, @AutoConfigureTestDatabase

**Example**:
```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AccountRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Test
    void shouldFindAccountByUpiId() {
        // Arrange
        Account account = new Account();
        account.setUpiId("alice@okaxis");
        account.setBalance(new BigDecimal("10000"));
        entityManager.persist(account);
        entityManager.flush();
        
        // Act
        Optional<Account> found = accountRepository.findByUpiId("alice@okaxis");
        
        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getBalance())
            .isEqualTo(new BigDecimal("10000"));
    }
    
    @Test
    void shouldReturnEmpty_WhenAccountNotFound() {
        // Act
        Optional<Account> found = accountRepository.findByUpiId("nonexistent@upi");
        
        // Assert
        assertThat(found).isEmpty();
    }
}
```

---

## Testing Edge Cases

### Boundary Value Testing

Test the boundaries of acceptable input:

```java
@Test
void shouldAcceptMinimumAmount() {
    BigDecimal minAmount = new BigDecimal("1");  // Boundary
    // Test passes
}

@Test
void shouldAcceptMaximumAmount() {
    BigDecimal maxAmount = new BigDecimal("100000");  // Boundary
    // Test passes
}

@Test
void shouldRejectAmountJustBelowMinimum() {
    BigDecimal belowMin = new BigDecimal("0.99");  // Just below boundary
    // Test expects exception
}

@Test
void shouldRejectAmountJustAboveMaximum() {
    BigDecimal aboveMax = new BigDecimal("100001");  // Just above boundary
    // Test expects exception
}
```

---

### Null Handling

Test null inputs:

```java
@Test
void shouldThrowException_WhenAmountIsNull() {
    TransferRequest request = new TransferRequest();
    request.setAmount(null);
    
    assertThatThrownBy(() -> service.transfer(request))
        .isInstanceOf(InvalidAmountException.class);
}

@Test
void shouldThrowException_WhenSourceUPIIsNull() {
    TransferRequest request = new TransferRequest();
    request.setSourceUPI(null);
    
    assertThatThrownBy(() -> service.transfer(request))
        .isInstanceOf(InvalidUpiException.class);
}
```

---

### Empty Collections

```java
@Test
void shouldReturnEmptyList_WhenNoTransactionsExist() {
    // Arrange
    when(transactionRepository.findByUpiId("alice@okaxis"))
        .thenReturn(Collections.emptyList());
    
    // Act
    List<Transaction> transactions = service.getTransactions("alice@okaxis");
    
    // Assert
    assertThat(transactions).isEmpty();
}
```

---

## Parameterized Tests

Test multiple scenarios with one test:

```java
@ParameterizedTest
@ValueSource(strings = {"1", "500", "50000", "100000"})
@DisplayName("Should accept valid amounts")
void shouldAcceptValidAmounts(String amountStr) {
    BigDecimal amount = new BigDecimal(amountStr);
    
    assertThatCode(() -> validator.validateAmount(amount))
        .doesNotThrowAnyException();
}

@ParameterizedTest
@CsvSource({
    "100, 0",      // Below threshold, no fee
    "500, 0",      // Below threshold, no fee
    "1000, 0",     // At threshold, no fee
    "1001, 5",     // Above threshold, fee applies
    "5000, 5",     // Above threshold, fee applies
    "100000, 5"    // Above threshold, fee applies
})
void shouldCalculateFeeCorrectly(String amountStr, String expectedFeeStr) {
    BigDecimal amount = new BigDecimal(amountStr);
    BigDecimal expectedFee = new BigDecimal(expectedFeeStr);
    
    BigDecimal actualFee = feeCalculator.calculateFee(amount);
    
    assertThat(actualFee).isEqualTo(expectedFee);
}

@ParameterizedTest
@MethodSource("invalidTransferScenarios")
void shouldRejectInvalidTransfers(TransferRequest request, Class<? extends Exception> expectedException) {
    assertThatThrownBy(() -> service.transfer(request))
        .isInstanceOf(expectedException);
}

static Stream<Arguments> invalidTransferScenarios() {
    return Stream.of(
        Arguments.of(
            new TransferRequest("alice@okaxis", "alice@okaxis", new BigDecimal("500"), ""),
            InvalidTransferException.class
        ),
        Arguments.of(
            new TransferRequest("alice@okaxis", "bob@paytm", new BigDecimal("0.50"), ""),
            InvalidAmountException.class
        ),
        Arguments.of(
            new TransferRequest("alice@okaxis", "bob@paytm", new BigDecimal("200000"), ""),
            InvalidAmountException.class
        )
    );
}
```

---

## Mocking Strategies

### When to Mock

**DO Mock**:
- External dependencies (database, APIs, file system)
- Complex dependencies
- Dependencies not under test

**DON'T Mock**:
- Simple value objects (DTOs, entities)
- Code under test
- Everything (over-mocking makes tests brittle)

---

### Mockito Patterns

**Return Values**:
```java
when(repository.findById(1L))
    .thenReturn(Optional.of(account));
```

**Throw Exceptions**:
```java
when(repository.save(any()))
    .thenThrow(new DataAccessException("Database error"));
```

**Verify Interactions**:
```java
verify(repository).save(account);
verify(repository, times(2)).save(any());
verify(repository, never()).delete(any());
```

**Argument Captors**:
```java
ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
verify(repository).save(captor.capture());

Account savedAccount = captor.getValue();
assertThat(savedAccount.getBalance()).isEqualTo(expected);
```

**Answer with Custom Logic**:
```java
when(repository.save(any(Account.class)))
    .thenAnswer(invocation -> {
        Account account = invocation.getArgument(0);
        account.setId(123L);  // Simulate ID generation
        return account;
    });
```

---

## Test Data Builders

Create reusable test data:

```java
public class AccountBuilder {
    
    private String upiId = "test@upi";
    private BigDecimal balance = new BigDecimal("10000");
    private String status = "ACTIVE";
    
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
    
    public AccountBuilder inactive() {
        this.status = "INACTIVE";
        return this;
    }
    
    public Account build() {
        Account account = new Account();
        account.setUpiId(upiId);
        account.setBalance(balance);
        account.setStatus(status);
        return account;
    }
}

// Usage
Account account = AccountBuilder.anAccount()
    .withUpiId("alice@okaxis")
    .withLowBalance()
    .build();
```

---

## AssertJ Assertions

**Better than JUnit assertions**:

```java
// JUnit - Not fluent
assertEquals("SUCCESS", response.getStatus());
assertTrue(response.getAmount().compareTo(BigDecimal.ZERO) > 0);

// AssertJ - Fluent and readable
assertThat(response.getStatus()).isEqualTo("SUCCESS");
assertThat(response.getAmount()).isPositive();

// Exception assertions
assertThatThrownBy(() -> service.transfer(request))
    .isInstanceOf(InsufficientBalanceException.class)
    .hasMessageContaining("Insufficient balance")
    .hasNoCause();

// Collection assertions
assertThat(transactions)
    .hasSize(5)
    .extracting("amount")
    .contains(new BigDecimal("500"));

// Object assertions
assertThat(account)
    .extracting("upiId", "balance", "status")
    .containsExactly("alice@okaxis", new BigDecimal("10000"), "ACTIVE");
```

---

## Test Organization

### Package Structure

```
src/test/java/
└── com/npci/transfer/
    ├── controller/
    │   └── TransferControllerTest.java
    ├── service/
    │   ├── TransferServiceTest.java
    │   └── FeeCalculatorTest.java
    ├── repository/
    │   └── AccountRepositoryTest.java
    ├── util/
    │   └── TestDataBuilder.java
    └── integration/
        └── TransferIntegrationTest.java
```

---

### Test Class Naming

- Test class: `ClassUnderTest` + `Test`
- Example: `TransferService` → `TransferServiceTest`

---

### Test Method Organization

```java
class TransferServiceTest {
    
    // ========== Setup ==========
    @BeforeEach
    void setUp() { }
    
    // ========== Happy Path Tests ==========
    @Test
    void shouldTransferSuccessfully() { }
    
    // ========== Error Path Tests ==========
    @Test
    void shouldThrowException_WhenInsufficientBalance() { }
    
    // ========== Edge Case Tests ==========
    @Test
    void shouldHandleMinimumAmount() { }
    
    // ========== Helper Methods ==========
    private Account createTestAccount() { }
}
```

---

## Coverage Goals

### Target Coverage

- **Line Coverage**: >90%
- **Branch Coverage**: >85%
- **Method Coverage**: >90%

### What NOT to Cover

- Getters/Setters (Lombok)
- Trivial constructors
- Framework code
- Configuration classes

### How to Check Coverage

```bash
mvn clean test jacoco:report
open target/site/jacoco/index.html
```

---

## Common Mistakes

### Mistake 1: Testing Implementation

**Bad**:
```java
@Test
void shouldCallRepositorySaveMethod() {
    service.transfer(request);
    verify(repository).save(any());  // Testing HOW, not WHAT
}
```

**Good**:
```java
@Test
void shouldUpdateAccountBalance() {
    service.transfer(request);
    assertThat(account.getBalance()).isEqualTo(expected);  // Testing WHAT
}
```

---

### Mistake 2: Too Many Assertions

**Bad**:
```java
@Test
void shouldDoEverything() {
    // 20 assertions - hard to debug when it fails
}
```

**Good**:
```java
@Test
void shouldSetStatusToSuccess() {
    assertThat(response.getStatus()).isEqualTo("SUCCESS");
}

@Test
void shouldDeductCorrectAmount() {
    assertThat(source.getBalance()).isEqualTo(expected);
}
```

---

### Mistake 3: Dependent Tests

**Bad**:
```java
private static Account sharedAccount;

@Test
void test1_createAccount() {
    sharedAccount = service.create();
}

@Test
void test2_updateAccount() {
    service.update(sharedAccount);  // Depends on test1!
}
```

**Good**:
```java
@Test
void shouldCreateAccount() {
    Account account = service.create();  // Independent
}

@Test
void shouldUpdateAccount() {
    Account account = createTestAccount();  // Independent
    service.update(account);
}
```

---

### Mistake 4: Not Testing Edge Cases

**Bad**:
```java
@Test
void shouldTransfer() {
    // Only tests happy path
}
```

**Good**:
```java
@Test
void shouldTransferSuccessfully() { }

@Test
void shouldHandleMinimumAmount() { }

@Test
void shouldHandleMaximumAmount() { }

@Test
void shouldHandleNullAmount() { }

@Test
void shouldHandleNegativeAmount() { }
```

---

## Test Documentation

### Use @DisplayName

```java
@Test
@DisplayName("Should transfer ₹500 from Alice to Bob when Alice has ₹10,000 balance")
void shouldTransferSuccessfully() {
    // Test implementation
}
```

### Document Complex Logic

```java
@Test
void shouldCalculateFeeCorrectly() {
    // Given: Amount > ₹1,000 triggers ₹5 fee
    // When: Transfer ₹1,500
    // Then: Fee should be ₹5, total debit ₹1,505
    
    // Test implementation
}
```

---

## Performance

### Fast Tests

- Unit test: <100ms
- Test suite: <10 seconds
- No real database
- No network calls
- No file I/O

### Slow Test Warning

If tests are slow:
1. Check for database access (use mocks)
2. Check for Thread.sleep()
3. Check for network calls
4. Check for complex setup

---

## Next Steps

1. ✅ Understand testing principles
2. ✅ Learn layer-specific testing
3. ⏳ Achieve >90% coverage
4. ⏳ Write tests for all edge cases
5. ⏳ Refactor with confidence
