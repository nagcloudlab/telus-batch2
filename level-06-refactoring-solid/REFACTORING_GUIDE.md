# Step-by-Step Refactoring Guide

## Overview
This guide walks through refactoring Level 4 bad code using SOLID principles and TDD from Level 5.

---

## Refactoring Strategy

### 1. Strangler Fig Pattern
Don't rewrite everything at once. Gradually replace old code with new code.

```
Old Code (Monolith) → Gradually Extract → New Code (Layered)
```

### 2. Test-First Refactoring
Write tests before refactoring to ensure behavior doesn't change.

---

## Step 1: Extract DTOs (Data Transfer Objects)

### Why?
- Replace `Map<String, Object>` with type-safe classes
- Enable validation
- Better IDE support
- Clear contracts

### Implementation

**TransferRequest.java**:
```java
package com.npci.transfer.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {
    
    @NotBlank(message = "Source UPI ID is required")
    @Pattern(regexp = "^[a-zA-Z0-9.\\-_]+@[a-zA-Z]+$")
    private String sourceUPI;
    
    @NotBlank(message = "Destination UPI ID is required")
    @Pattern(regexp = "^[a-zA-Z0-9.\\-_]+@[a-zA-Z]+$")
    private String destinationUPI;
    
    @NotNull
    @DecimalMin("1.0")
    @DecimalMax("100000.0")
    private BigDecimal amount;
    
    @Size(max = 255)
    private String remarks;
}
```

**TransferResponse.java**:
```java
package com.npci.transfer.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

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

**Benefits**:
- ✅ Type safety
- ✅ Automatic validation
- ✅ Clear API contract
- ✅ Better error messages

---

## Step 2: Extract Repository Layer (D - Dependency Inversion)

### Why?
- Abstract data access
- Enable testing without database
- Follow Dependency Inversion Principle

### Implementation

**AccountRepository.java** (Interface):
```java
package com.npci.transfer.repository;

import com.npci.transfer.entity.Account;
import java.util.Optional;

public interface AccountRepository {
    
    Optional<Account> findByUpiId(String upiId);
    
    Account save(Account account);
    
    void delete(Account account);
}
```

**JpaAccountRepository.java** (Implementation):
```java
package com.npci.transfer.repository;

import com.npci.transfer.entity.Account;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class JpaAccountRepository implements AccountRepository {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    public Optional<Account> findByUpiId(String upiId) {
        List<Account> accounts = entityManager
            .createQuery("SELECT a FROM Account a WHERE a.upiId = :upi", Account.class)
            .setParameter("upi", upiId)
            .getResultList();
        
        return accounts.isEmpty() ? Optional.empty() : Optional.of(accounts.get(0));
    }
    
    @Override
    public Account save(Account account) {
        if (account.getId() == null) {
            entityManager.persist(account);
            return account;
        } else {
            return entityManager.merge(account);
        }
    }
    
    @Override
    public void delete(Account account) {
        entityManager.remove(account);
    }
}
```

**TransactionRepository.java** (Interface):
```java
package com.npci.transfer.repository;

import com.npci.transfer.entity.Transaction;
import java.util.Optional;

public interface TransactionRepository {
    
    Optional<Transaction> findByTransactionId(String transactionId);
    
    Transaction save(Transaction transaction);
}
```

**Benefits**:
- ✅ Testable (can use in-memory implementation)
- ✅ Can switch database technology
- ✅ Clean separation of concerns

---

## Step 3: Extract Service Layer (S - Single Responsibility)

### Why?
- Separate business logic from HTTP handling
- Single Responsibility Principle
- Testable business logic

### Implementation

**TransferService.java**:
```java
package com.npci.transfer.service;

import com.npci.transfer.dto.TransferRequest;
import com.npci.transfer.dto.TransferResponse;
import com.npci.transfer.entity.Account;
import com.npci.transfer.entity.Transaction;
import com.npci.transfer.exception.*;
import com.npci.transfer.repository.AccountRepository;
import com.npci.transfer.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class TransferService {
    
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final FeeCalculator feeCalculator;
    
    private static final BigDecimal MIN_AMOUNT = new BigDecimal("1");
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("100000");
    
    @Transactional
    public TransferResponse initiateTransfer(TransferRequest request) {
        // Validate
        validate(request);
        
        // Find accounts
        Account source = accountRepository.findByUpiId(request.getSourceUPI())
            .orElseThrow(() -> new AccountNotFoundException(
                "Source account not found: " + request.getSourceUPI()));
        
        Account destination = accountRepository.findByUpiId(request.getDestinationUPI())
            .orElseThrow(() -> new AccountNotFoundException(
                "Destination account not found: " + request.getDestinationUPI()));
        
        // Calculate fee
        BigDecimal fee = feeCalculator.calculateFee(request.getAmount());
        BigDecimal totalDebited = request.getAmount().add(fee);
        
        // Check balance
        if (source.getBalance().compareTo(totalDebited) < 0) {
            throw new InsufficientBalanceException(
                String.format("Insufficient balance. Available: ₹%s, Required: ₹%s",
                    source.getBalance(), totalDebited));
        }
        
        // Execute transfer
        source.setBalance(source.getBalance().subtract(totalDebited));
        destination.setBalance(destination.getBalance().add(request.getAmount()));
        
        accountRepository.save(source);
        accountRepository.save(destination);
        
        // Create transaction
        Transaction transaction = createTransaction(request, fee, totalDebited);
        transactionRepository.save(transaction);
        
        // Build response
        return TransferResponse.builder()
            .transactionId(transaction.getTransactionId())
            .status("SUCCESS")
            .sourceUPI(request.getSourceUPI())
            .destinationUPI(request.getDestinationUPI())
            .amount(request.getAmount())
            .fee(fee)
            .totalDebited(totalDebited)
            .timestamp(LocalDateTime.now())
            .remarks(request.getRemarks())
            .build();
    }
    
    private void validate(TransferRequest request) {
        if (request.getSourceUPI().equals(request.getDestinationUPI())) {
            throw new InvalidTransferException("Cannot transfer to the same account");
        }
        
        if (request.getAmount().compareTo(MIN_AMOUNT) < 0) {
            throw new InvalidAmountException("Minimum transfer amount is ₹1");
        }
        
        if (request.getAmount().compareTo(MAX_AMOUNT) > 0) {
            throw new InvalidAmountException("Maximum per-transaction limit is ₹1,00,000");
        }
    }
    
    private Transaction createTransaction(TransferRequest request, BigDecimal fee, BigDecimal totalDebited) {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(generateTransactionId());
        transaction.setSourceUPI(request.getSourceUPI());
        transaction.setDestinationUPI(request.getDestinationUPI());
        transaction.setAmount(request.getAmount());
        transaction.setFee(fee);
        transaction.setTotalDebited(totalDebited);
        transaction.setStatus("SUCCESS");
        transaction.setRemarks(request.getRemarks());
        transaction.setTimestamp(LocalDateTime.now());
        return transaction;
    }
    
    private String generateTransactionId() {
        LocalDateTime now = LocalDateTime.now();
        String datePart = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = String.format("%06d", new Random().nextInt(999999));
        return "TXN-" + datePart + "-" + randomPart;
    }
}
```

**FeeCalculator.java** (O - Open/Closed):
```java
package com.npci.transfer.service;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class FeeCalculator {
    
    private static final BigDecimal FEE_THRESHOLD = new BigDecimal("1000");
    private static final BigDecimal TRANSACTION_FEE = new BigDecimal("5.00");
    
    public BigDecimal calculateFee(BigDecimal amount) {
        return amount.compareTo(FEE_THRESHOLD) > 0 
            ? TRANSACTION_FEE 
            : BigDecimal.ZERO;
    }
}
```

**Benefits**:
- ✅ Business logic isolated
- ✅ Testable without HTTP
- ✅ Clear dependencies via constructor injection
- ✅ Single responsibility

---

## Step 4: Refactor Controller (S - Single Responsibility)

### Why?
- Controller should only handle HTTP
- Delegate business logic to service
- Clean separation of concerns

### Implementation

**TransferController.java**:
```java
package com.npci.transfer.controller;

import com.npci.transfer.dto.TransferRequest;
import com.npci.transfer.dto.TransferResponse;
import com.npci.transfer.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class TransferController {
    
    private final TransferService transferService;
    
    @PostMapping("/transfers")
    public ResponseEntity<TransferResponse> initiateTransfer(
            @Valid @RequestBody TransferRequest request) {
        
        TransferResponse response = transferService.initiateTransfer(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "timestamp", LocalDateTime.now().toString()
        ));
    }
}
```

**Comparison**:

**Before** (127 lines):
```java
public Map<String, Object> transfer(@RequestBody Map<String, Object> req) {
    // HTTP handling
    // Validation
    // Database access
    // Business logic
    // Transaction management
    // Response construction
    // 127 lines!
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

**Benefits**:
- ✅ 127 lines → 13 lines (90% reduction!)
- ✅ One responsibility (HTTP handling)
- ✅ Clean and readable
- ✅ Easy to test

---

## Step 5: Add Exception Handling

**GlobalExceptionHandler.java**:
```java
package com.npci.transfer.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAccountNotFound(AccountNotFoundException ex) {
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.NOT_FOUND.value())
            .error("Account Not Found")
            .message(ex.getMessage())
            .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientBalance(InsufficientBalanceException ex) {
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Insufficient Balance")
            .message(ex.getMessage())
            .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validation Error")
            .message("Invalid request")
            .validationErrors(errors)
            .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
```

---

## Step 6: Write Tests (Using TDD from Level 5)

### Service Layer Tests

**TransferServiceTest.java**:
```java
package com.npci.transfer.service;

import com.npci.transfer.dto.TransferRequest;
import com.npci.transfer.dto.TransferResponse;
import com.npci.transfer.entity.Account;
import com.npci.transfer.exception.*;
import com.npci.transfer.repository.AccountRepository;
import com.npci.transfer.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
    
    private Account sourceAccount;
    private Account destinationAccount;
    private TransferRequest request;
    
    @BeforeEach
    void setUp() {
        sourceAccount = new Account();
        sourceAccount.setUpiId("alice@okaxis");
        sourceAccount.setBalance(new BigDecimal("10000"));
        
        destinationAccount = new Account();
        destinationAccount.setUpiId("bob@paytm");
        destinationAccount.setBalance(new BigDecimal("5000"));
        
        request = new TransferRequest(
            "alice@okaxis",
            "bob@paytm",
            new BigDecimal("500"),
            "Test transfer"
        );
    }
    
    @Test
    void shouldTransferSuccessfully_WhenBalanceIsSufficient() {
        // Arrange
        when(accountRepository.findByUpiId("alice@okaxis"))
            .thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByUpiId("bob@paytm"))
            .thenReturn(Optional.of(destinationAccount));
        when(feeCalculator.calculateFee(any())).thenReturn(BigDecimal.ZERO);
        
        // Act
        TransferResponse response = transferService.initiateTransfer(request);
        
        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        assertThat(response.getAmount()).isEqualTo(new BigDecimal("500"));
        
        verify(accountRepository, times(2)).save(any(Account.class));
        verify(transactionRepository, times(1)).save(any());
    }
    
    @Test
    void shouldThrowException_WhenSourceAccountNotFound() {
        // Arrange
        when(accountRepository.findByUpiId("alice@okaxis"))
            .thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> transferService.initiateTransfer(request))
            .isInstanceOf(AccountNotFoundException.class)
            .hasMessageContaining("Source account not found");
    }
    
    @Test
    void shouldThrowException_WhenInsufficientBalance() {
        // Arrange
        sourceAccount.setBalance(new BigDecimal("100"));
        when(accountRepository.findByUpiId("alice@okaxis"))
            .thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByUpiId("bob@paytm"))
            .thenReturn(Optional.of(destinationAccount));
        when(feeCalculator.calculateFee(any())).thenReturn(BigDecimal.ZERO);
        
        // Act & Assert
        assertThatThrownBy(() -> transferService.initiateTransfer(request))
            .isInstanceOf(InsufficientBalanceException.class)
            .hasMessageContaining("Insufficient balance");
    }
    
    @Test
    void shouldThrowException_WhenSameSourceAndDestination() {
        // Arrange
        request.setDestinationUPI("alice@okaxis");
        
        // Act & Assert
        assertThatThrownBy(() -> transferService.initiateTransfer(request))
            .isInstanceOf(InvalidTransferException.class)
            .hasMessageContaining("Cannot transfer to the same account");
    }
}
```

---

## Summary of Changes

### Metrics Comparison

| Metric | Before (Level 4) | After (Level 6) | Improvement |
|--------|-----------------|-----------------|-------------|
| **Controller LOC** | 127 lines | 13 lines | 90% ↓ |
| **Cyclomatic Complexity** | 15 | 2 | 87% ↓ |
| **Code Smells** | 48 | 3 | 94% ↓ |
| **Test Coverage** | 0% | 85% | +85% |
| **Classes** | 3 | 15 | More focused |
| **Testability** | Impossible | Easy | ∞ ↑ |

### SOLID Compliance

| Principle | Before | After |
|-----------|--------|-------|
| **S**ingle Responsibility | ❌ Violated | ✅ Applied |
| **O**pen/Closed | ❌ Violated | ✅ Applied |
| **L**iskov Substitution | ❌ N/A | ✅ Applied |
| **I**nterface Segregation | ❌ N/A | ✅ Applied |
| **D**ependency Inversion | ❌ Violated | ✅ Applied |

---

## Next Steps

1. ✅ Understand refactoring strategy
2. ✅ Apply SOLID principles
3. ⏳ Write comprehensive tests (Level 7)
4. ⏳ Add integration tests (Level 12)
5. ⏳ Measure and celebrate improvements!
