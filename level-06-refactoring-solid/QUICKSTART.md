# Level 6: Quick Start Guide

## 🚀 Refactor Bad Code in 2 Hours

This guide helps you transform Level 4 bad code into clean, SOLID-compliant architecture.

---

## Prerequisites

```bash
# Have Level 4 project ready
cd level-04-project-setup-bad-code

# Java 17+, Maven 3.8+
java --version
mvn --version
```

---

## Step 1: Understand the Problem (10 minutes)

### Review Level 4 Bad Code

Open `TransferController.java`:

```java
// 127 lines of EVERYTHING:
@PostMapping("/transfers")
public Map<String, Object> transfer(@RequestBody Map<String, Object> req) {
    // HTTP handling
    // Validation
    // Database access
    // Business logic
    // Response construction
    // 6 responsibilities! ❌
}
```

**Problems**:
- ❌ God Object (does everything)
- ❌ Untestable (direct EntityManager)
- ❌ No separation of concerns
- ❌ Hardcoded logic
- ❌ Poor naming

---

## Step 2: Create DTOs (15 minutes)

### Replace Map<String, Object> with Type-Safe Classes

**Create** `src/main/java/com/npci/transfer/dto/TransferRequest.java`:

```java
package com.npci.transfer.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {
    
    @NotBlank(message = "Source UPI ID is required")
    private String sourceUPI;
    
    @NotBlank(message = "Destination UPI ID is required")
    private String destinationUPI;
    
    @NotNull
    @DecimalMin("1.0")
    @DecimalMax("100000.0")
    private BigDecimal amount;
    
    private String remarks;
}
```

**Create** `TransferResponse.java`:

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponse {
    private String transactionId;
    private String status;
    private String sourceUPI;
    private String destinationUPI;
    private BigDecimal amount;
    private BigDecimal fee;
    private BigDecimal totalDebited;
    private LocalDateTime timestamp;
    private String remarks;
}
```

---

## Step 3: Extract Repository Layer (20 minutes)

### Create Interfaces (D - Dependency Inversion)

**Create** `src/main/java/com/npci/transfer/repository/AccountRepository.java`:

```java
package com.npci.transfer.repository;

import com.npci.transfer.entity.Account;
import java.util.Optional;

public interface AccountRepository {
    Optional<Account> findByUpiId(String upiId);
    Account save(Account account);
}
```

### Implement Repository

**Create** `JpaAccountRepository.java`:

```java
@Repository
public class JpaAccountRepository implements AccountRepository {
    
    @PersistenceContext
    private EntityManager em;
    
    @Override
    public Optional<Account> findByUpiId(String upiId) {
        List<Account> accounts = em
            .createQuery("SELECT a FROM Account a WHERE a.upiId = :upi", Account.class)
            .setParameter("upi", upiId)
            .getResultList();
        return accounts.isEmpty() ? Optional.empty() : Optional.of(accounts.get(0));
    }
    
    @Override
    public Account save(Account account) {
        if (account.getId() == null) {
            em.persist(account);
            return account;
        }
        return em.merge(account);
    }
}
```

**Do the same for TransactionRepository!**

---

## Step 4: Extract Service Layer (30 minutes)

### Create Service (S - Single Responsibility)

**Create** `src/main/java/com/npci/transfer/service/TransferService.java`:

```java
@Service
@RequiredArgsConstructor  // Lombok constructor injection
public class TransferService {
    
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final FeeCalculator feeCalculator;
    
    @Transactional
    public TransferResponse initiateTransfer(TransferRequest request) {
        // 1. Validate
        validate(request);
        
        // 2. Find accounts
        Account source = accountRepository.findByUpiId(request.getSourceUPI())
            .orElseThrow(() -> new AccountNotFoundException("Source not found"));
        
        Account destination = accountRepository.findByUpiId(request.getDestinationUPI())
            .orElseThrow(() -> new AccountNotFoundException("Destination not found"));
        
        // 3. Calculate fee
        BigDecimal fee = feeCalculator.calculateFee(request.getAmount());
        BigDecimal total = request.getAmount().add(fee);
        
        // 4. Check balance
        if (source.getBalance().compareTo(total) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }
        
        // 5. Execute transfer
        source.setBalance(source.getBalance().subtract(total));
        destination.setBalance(destination.getBalance().add(request.getAmount()));
        
        accountRepository.save(source);
        accountRepository.save(destination);
        
        // 6. Create transaction
        Transaction txn = createTransaction(request, fee, total);
        transactionRepository.save(txn);
        
        // 7. Build response
        return TransferResponse.builder()
            .transactionId(txn.getTransactionId())
            .status("SUCCESS")
            .sourceUPI(request.getSourceUPI())
            .destinationUPI(request.getDestinationUPI())
            .amount(request.getAmount())
            .fee(fee)
            .totalDebited(total)
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    private void validate(TransferRequest request) {
        if (request.getSourceUPI().equals(request.getDestinationUPI())) {
            throw new InvalidTransferException("Cannot transfer to same account");
        }
    }
    
    private Transaction createTransaction(TransferRequest req, BigDecimal fee, BigDecimal total) {
        Transaction txn = new Transaction();
        txn.setTransactionId(generateTransactionId());
        txn.setSourceUPI(req.getSourceUPI());
        txn.setDestinationUPI(req.getDestinationUPI());
        txn.setAmount(req.getAmount());
        txn.setFee(fee);
        txn.setTotalDebited(total);
        txn.setStatus("SUCCESS");
        txn.setTimestamp(LocalDateTime.now());
        return txn;
    }
    
    private String generateTransactionId() {
        return "TXN-" + System.currentTimeMillis();
    }
}
```

---

## Step 5: Refactor Controller (10 minutes)

### Slim Down Controller

**Update** `TransferController.java`:

```java
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class TransferController {
    
    private final TransferService transferService;  // Only dependency!
    
    @PostMapping("/transfers")
    public ResponseEntity<TransferResponse> initiateTransfer(
            @Valid @RequestBody TransferRequest request) {
        
        TransferResponse response = transferService.initiateTransfer(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }
}
```

**From 127 lines → 13 lines!** 🎉

---

## Step 6: Write Tests (30 minutes)

### Test Service Layer

**Create** `src/test/java/com/npci/transfer/service/TransferServiceTest.java`:

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
    private TransferService service;
    
    @Test
    void shouldTransferSuccessfully() {
        // Arrange
        Account source = new Account();
        source.setUpiId("alice@okaxis");
        source.setBalance(new BigDecimal("10000"));
        
        Account dest = new Account();
        dest.setUpiId("bob@paytm");
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
        TransferResponse response = service.initiateTransfer(request);
        
        // Assert
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        assertThat(response.getAmount()).isEqualTo(new BigDecimal("500"));
        
        verify(accountRepository, times(2)).save(any());
    }
    
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
        assertThatThrownBy(() -> service.initiateTransfer(request))
            .isInstanceOf(InsufficientBalanceException.class);
    }
}
```

---

## Step 7: Run and Verify (5 minutes)

```bash
# Run tests
mvn test

# Expected:
# Tests run: 10+, Failures: 0 ✅

# Check coverage
mvn jacoco:report
open target/site/jacoco/index.html

# Expected:
# Service Layer: >85% coverage ✅
```

---

## Before vs After Comparison

### Controller Code

**Before** (127 lines):
```java
public Map<String, Object> transfer(@RequestBody Map<String, Object> req) {
    String src = (String) req.get("sourceUPI");  // Poor naming
    
    List<Account> accounts = em.createQuery(...);  // Direct DB access
    Account source = accounts.get(0);  // No null check
    
    BigDecimal fee = amount > 1000 ? 5.0 : 0.0;  // Hardcoded logic
    
    // 120 more lines...
}
```

**After** (13 lines):
```java
public ResponseEntity<TransferResponse> initiateTransfer(
        @Valid @RequestBody TransferRequest request) {
    
    TransferResponse response = transferService.initiateTransfer(request);
    return ResponseEntity.ok(response);
}
```

**Improvements**:
- ✅ 90% fewer lines
- ✅ Type-safe
- ✅ Validated automatically
- ✅ Clean and readable
- ✅ One responsibility

---

## Checklist

- [ ] DTOs created (TransferRequest, TransferResponse)
- [ ] Repository interfaces created
- [ ] Repository implementations created
- [ ] Service layer created
- [ ] Controller refactored
- [ ] Tests written for service
- [ ] All tests passing
- [ ] Coverage >80%

---

## Common Issues

### Issue: Lombok not working

**Solution**:
```xml
<!-- Add to pom.xml -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>

<!-- Enable annotation processing in IDE -->
```

### Issue: Validation not working

**Solution**:
```xml
<!-- Add to pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

### Issue: Tests fail with "No qualifying bean"

**Solution**:
```java
// Use @ExtendWith(MockitoExtension.class)
// NOT @SpringBootTest for unit tests
```

---

## Metrics to Celebrate

| Metric | Before | After |
|--------|--------|-------|
| **Controller LOC** | 127 | 13 |
| **Complexity** | 15 | 2 |
| **Code Smells** | 48 | 3 |
| **Coverage** | 0% | 85% |
| **Testability** | Impossible | Easy |

**Quality**: 🔴 2/10 → ✅ 8/10

---

## What You Learned

### SOLID Principles Applied
✅ **S**: Controller, Service, Repository separated  
✅ **O**: FeeCalculator with strategy pattern  
✅ **L**: Repository implementations interchangeable  
✅ **I**: Focused interfaces  
✅ **D**: Service depends on abstractions  

### Skills Gained
✅ Refactoring techniques  
✅ Layer architecture  
✅ Dependency injection  
✅ Testing with mocks  
✅ Clean code practices  

---

## Next Steps

1. ✅ Level 6 Complete - SOLID Applied
2. ⏳ Level 7: Comprehensive Unit Testing
3. ⏳ Level 8: Integration Testing
4. ⏳ Level 9: Performance Testing

---

**Time Spent**: ~2 hours  
**Quality Improvement**: 300%  
**SOLID Compliance**: 100%  
**Feeling**: 🚀 Confident!

🎉 **Congratulations!** You've transformed bad code into professional-quality code!
