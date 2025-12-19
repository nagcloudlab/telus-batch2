# Complete Testing Examples

This document shows complete, working examples of all testing patterns used in Level 7.

---

## Example 1: Controller Test with MockMvc

```java
@WebMvcTest(TransferController.class)
class TransferControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private TransferService transferService;
    
    @Test
    void shouldReturn200_WhenTransferSuccessful() throws Exception {
        // Arrange - Setup mock response
        TransferResponse response = TransferResponse.builder()
            .transactionId("TXN-123")
            .status("SUCCESS")
            .sourceUPI("alice@okaxis")
            .destinationUPI("bob@paytm")
            .amount(new BigDecimal("500"))
            .fee(BigDecimal.ZERO)
            .totalDebited(new BigDecimal("500"))
            .timestamp(LocalDateTime.now())
            .build();
        
        when(transferService.initiateTransfer(any(TransferRequest.class)))
            .thenReturn(response);
        
        // Act & Assert - Make HTTP request and verify response
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
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.transactionId").value("TXN-123"))
            .andExpect(jsonPath("$.status").value("SUCCESS"))
            .andExpect(jsonPath("$.amount").value(500));
        
        // Verify service was called once
        verify(transferService, times(1)).initiateTransfer(any());
    }
}
```

**What it tests**:
- ✅ HTTP status code (200)
- ✅ Response content type (JSON)
- ✅ Response body structure
- ✅ Service method invocation

---

## Example 2: Service Test with Mockito

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
        // Arrange - Create test data
        Account source = new Account();
        source.setUpiId("alice@okaxis");
        source.setBalance(new BigDecimal("10000"));
        
        Account destination = new Account();
        destination.setUpiId("bob@paytm");
        destination.setBalance(new BigDecimal("5000"));
        
        // Setup mocks
        when(accountRepository.findByUpiId("alice@okaxis"))
            .thenReturn(Optional.of(source));
        when(accountRepository.findByUpiId("bob@paytm"))
            .thenReturn(Optional.of(destination));
        when(feeCalculator.calculateFee(any()))
            .thenReturn(BigDecimal.ZERO);
        when(accountRepository.save(any(Account.class)))
            .thenAnswer(i -> i.getArguments()[0]);
        
        TransferRequest request = new TransferRequest(
            "alice@okaxis", "bob@paytm", new BigDecimal("500"), "Test"
        );
        
        // Act - Execute service method
        TransferResponse response = transferService.initiateTransfer(request);
        
        // Assert - Verify results
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        assertThat(response.getAmount()).isEqualTo(new BigDecimal("500"));
        
        // Verify state changes
        assertThat(source.getBalance()).isEqualTo(new BigDecimal("9500"));
        assertThat(destination.getBalance()).isEqualTo(new BigDecimal("5500"));
        
        // Verify interactions
        verify(accountRepository, times(2)).save(any(Account.class));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }
}
```

**What it tests**:
- ✅ Business logic execution
- ✅ State changes (balance updates)
- ✅ Method calls (save operations)
- ✅ Return values

---

## Example 3: Parameterized Test

```java
@ParameterizedTest
@CsvSource({
    "100, 0.00",      // Below threshold
    "500, 0.00",      // Below threshold
    "1000, 0.00",     // At threshold
    "1001, 5.00",     // Above threshold
    "5000, 5.00"      // Well above threshold
})
@DisplayName("Should calculate fee correctly for various amounts")
void shouldCalculateFeeCorrectly(String amountStr, String expectedFeeStr) {
    // Arrange
    BigDecimal amount = new BigDecimal(amountStr);
    BigDecimal expectedFee = new BigDecimal(expectedFeeStr);
    
    // Act
    BigDecimal actualFee = feeCalculator.calculateFee(amount);
    
    // Assert
    assertThat(actualFee).isEqualTo(expectedFee);
}
```

**What it tests**:
- ✅ 5 scenarios with 1 test method
- ✅ Fee calculation logic
- ✅ Boundary conditions
- ✅ Clear test matrix

---

## Example 4: Exception Testing

```java
@Test
void shouldThrowException_WhenInsufficientBalance() {
    // Arrange
    Account source = new Account();
    source.setBalance(new BigDecimal("100"));
    
    when(accountRepository.findByUpiId(any()))
        .thenReturn(Optional.of(source));
    when(feeCalculator.calculateFee(any()))
        .thenReturn(BigDecimal.ZERO);
    
    TransferRequest request = new TransferRequest(
        "alice@okaxis", "bob@paytm", new BigDecimal("500"), "Test"
    );
    
    // Act & Assert
    assertThatThrownBy(() -> transferService.initiateTransfer(request))
        .isInstanceOf(InsufficientBalanceException.class)
        .hasMessageContaining("Insufficient balance")
        .hasMessageContaining("Available: ₹100")
        .hasMessageContaining("Required: ₹500");
    
    // Verify no state changes occurred
    verify(accountRepository, never()).save(any());
}
```

**What it tests**:
- ✅ Exception type
- ✅ Exception message
- ✅ No side effects when exception thrown

---

## Example 5: Argument Captor

```java
@Test
void shouldSaveTransactionWithCorrectDetails() {
    // Arrange
    BigDecimal fee = new BigDecimal("5.00");
    
    when(accountRepository.findByUpiId(anyString()))
        .thenReturn(Optional.of(sourceAccount))
        .thenReturn(Optional.of(destinationAccount));
    when(feeCalculator.calculateFee(any())).thenReturn(fee);
    when(accountRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
    
    // Capture the transaction that gets saved
    ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
    when(transactionRepository.save(captor.capture()))
        .thenAnswer(i -> i.getArguments()[0]);
    
    TransferRequest request = new TransferRequest(
        "alice@okaxis", "bob@paytm", new BigDecimal("500"), "Test"
    );
    
    // Act
    transferService.initiateTransfer(request);
    
    // Assert - Verify captured transaction
    Transaction savedTransaction = captor.getValue();
    assertThat(savedTransaction.getSourceUPI()).isEqualTo("alice@okaxis");
    assertThat(savedTransaction.getDestinationUPI()).isEqualTo("bob@paytm");
    assertThat(savedTransaction.getAmount()).isEqualTo(new BigDecimal("500"));
    assertThat(savedTransaction.getFee()).isEqualTo(new BigDecimal("5.00"));
    assertThat(savedTransaction.getTotalDebited()).isEqualTo(new BigDecimal("505.00"));
    assertThat(savedTransaction.getStatus()).isEqualTo("SUCCESS");
}
```

**What it tests**:
- ✅ Exact object passed to repository
- ✅ All transaction fields correct
- ✅ Calculation accuracy

---

## Example 6: Repository Test with TestEntityManager

```java
@DataJpaTest
class AccountRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Test
    void shouldFindAccountByUpiId() {
        // Arrange - Persist test data
        Account account = new Account();
        account.setUpiId("alice@okaxis");
        account.setBalance(new BigDecimal("10000"));
        account.setPhone("9876543210");
        account.setStatus("ACTIVE");
        
        entityManager.persist(account);
        entityManager.flush();
        
        // Act - Query via repository
        Optional<Account> found = accountRepository.findByUpiId("alice@okaxis");
        
        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getUpiId()).isEqualTo("alice@okaxis");
        assertThat(found.get().getBalance()).isEqualTo(new BigDecimal("10000"));
    }
}
```

**What it tests**:
- ✅ Database queries
- ✅ Data persistence
- ✅ Query correctness

---

## Example 7: Test Data Builder

```java
// Define builder
public class AccountBuilder {
    private String upiId = "test@upi";
    private BigDecimal balance = new BigDecimal("10000");
    
    public static AccountBuilder anAccount() {
        return new AccountBuilder();
    }
    
    public AccountBuilder withUpiId(String upiId) {
        this.upiId = upiId;
        return this;
    }
    
    public AccountBuilder withLowBalance() {
        this.balance = new BigDecimal("100");
        return this;
    }
    
    public Account build() {
        Account account = new Account();
        account.setUpiId(upiId);
        account.setBalance(balance);
        return account;
    }
}

// Use in tests
@Test
void testWithBuilder() {
    // Readable and expressive
    Account alice = AccountBuilder.anAccount()
        .withUpiId("alice@okaxis")
        .withLowBalance()
        .build();
    
    Account bob = AccountBuilder.anAccount()
        .withUpiId("bob@paytm")
        .build();
}
```

**Benefits**:
- ✅ Reusable
- ✅ Readable
- ✅ Easy variations

---

## Running All Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=TransferServiceTest

# Run specific test method
mvn test -Dtest=TransferServiceTest#shouldTransferSuccessfully

# Run with coverage
mvn clean test jacoco:report

# Quick script
./run-tests.sh
```

---

## Coverage Verification

```bash
# Generate coverage report
mvn jacoco:report

# Open report (Mac)
open target/site/jacoco/index.html

# Open report (Linux)
xdg-open target/site/jacoco/index.html

# Check coverage threshold
mvn jacoco:check
```

---

## Expected Results

```
Tests run: 53
Failures: 0
Errors: 0
Skipped: 0

Line Coverage: 93%
Branch Coverage: 91%
Method Coverage: 95%

Time: 3.2 seconds
```

**All tests pass! ✅**
**Coverage targets met! 🎯**
