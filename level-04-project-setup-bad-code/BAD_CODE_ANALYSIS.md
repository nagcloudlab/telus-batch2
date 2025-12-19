# Bad Code Analysis

## Overview
This document catalogs all the bad practices intentionally included in the initial codebase. Each will be fixed in subsequent levels.

---

## Code Smells Catalog

### 1. God Object (TransferController)

**Problem**: Controller does everything - business logic, data access, validation, error handling.

**Evidence**:
```java
@RestController
public class TransferController {
    @PersistenceContext
    private EntityManager em;  // Direct DB access!
    
    public Map<String, Object> transfer(...) {
        // 150+ lines of code
        // Database queries
        // Business logic
        // Response construction
        // Everything!
    }
}
```

**Violations**:
- Single Responsibility Principle (SRP)
- Separation of Concerns
- Controller should only handle HTTP, not business logic

**Fix**: Level 6 - Extract Service and Repository layers

---

### 2. No Separation of Concerns

**Problem**: No layered architecture (Controller → Service → Repository).

**Current Structure**:
```
TransferController
    ├── HTTP handling
    ├── Business logic
    ├── Data access
    ├── Validation
    └── Error handling
```

**Should Be**:
```
Controller (HTTP only)
    ↓
Service (Business logic)
    ↓
Repository (Data access)
```

**Fix**: Level 6 - Apply SOLID principles

---

### 3. Direct Database Access in Controller

**Problem**: Controller directly uses EntityManager.

**Evidence**:
```java
@PersistenceContext
private EntityManager em;

List<Account> accounts = em.createQuery(
    "SELECT a FROM Account a WHERE a.upiId = :upi", Account.class)
    .setParameter("upi", src)
    .getResultList();
```

**Why Bad**:
- Tight coupling to JPA
- Untestable (requires real database)
- No abstraction
- Violates Dependency Inversion Principle

**Fix**: Level 6 - Create Repository interface

---

### 4. No Input Validation

**Problem**: Accepts any input without validation.

**Evidence**:
```java
String src = (String) req.get("sourceUPI");  // Could be null!
double amt = ((Number) req.get("amount")).doubleValue();  // Could crash!
```

**Missing Validations**:
- UPI format validation
- Amount min/max check
- Required field validation
- Same source/destination check

**Consequences**:
- NullPointerException
- Invalid data in database
- Security vulnerabilities

**Fix**: Level 6 - Add validation logic

---

### 5. No Error Handling

**Problem**: No try-catch, exceptions crash the application.

**Evidence**:
```java
Account sourceAccount = srcAccounts.get(0);  // IndexOutOfBoundsException!
Transaction t = txns.get(0);  // Crashes if not found!
```

**Missing**:
- Try-catch blocks
- Custom exceptions
- Meaningful error messages
- HTTP status codes (always 200!)

**Fix**: Level 6 - Add error handling

---

### 6. Magic Numbers (Hardcoded Values)

**Problem**: Numbers with no context scattered in code.

**Evidence**:
```java
BigDecimal fee = amount.compareTo(BigDecimal.valueOf(1000)) > 0 
    ? BigDecimal.valueOf(5.0)  // Magic number!
    : BigDecimal.ZERO;

String txnId = "TXN-" + ... + new Random().nextInt(999999);  // Magic!
```

**Should Be Constants**:
```java
private static final BigDecimal FEE_THRESHOLD = new BigDecimal("1000");
private static final BigDecimal TRANSACTION_FEE = new BigDecimal("5.00");
private static final BigDecimal MIN_AMOUNT = new BigDecimal("1");
private static final BigDecimal MAX_AMOUNT = new BigDecimal("100000");
```

**Fix**: Level 6 - Extract constants

---

### 7. Poor Variable Naming

**Problem**: Cryptic abbreviations, no context.

**Bad Names**:
```java
String src = ...;       // What is src?
String dst = ...;       // What is dst?
double amt = ...;       // Amount?
String rem = ...;       // Remainder? Remark?
Map<String, Object> req = ...;  // Generic
Map<String, Object> resp = ...;  // Generic
Transaction t = ...;    // Single letter
Account a = ...;        // Single letter
```

**Good Names**:
```java
String sourceUpiId = ...;
String destinationUpiId = ...;
BigDecimal transferAmount = ...;
String transactionRemarks = ...;
TransferRequest request = ...;
TransferResponse response = ...;
Transaction transaction = ...;
Account account = ...;
```

**Fix**: Level 6 - Rename variables

---

### 8. Long Method (>100 lines)

**Problem**: `transfer()` method does too much.

**Cyclomatic Complexity**: ~15 (high)

**Should Be Broken Into**:
```
validateRequest()
checkBalance()
calculateFee()
debitSource()
creditDestination()
createTransaction()
buildResponse()
```

**Fix**: Level 6 - Extract methods

---

### 9. Primitive Obsession

**Problem**: Using Map<String, Object> instead of proper DTOs.

**Evidence**:
```java
public Map<String, Object> transfer(@RequestBody Map<String, Object> req)
```

**Problems**:
- No type safety
- Runtime errors (ClassCastException)
- No validation
- Poor IDE support
- No documentation

**Should Be**:
```java
public TransferResponse transfer(@RequestBody @Valid TransferRequest request)
```

**Fix**: Level 6 - Create DTOs

---

### 10. No Transaction Management Logic

**Problem**: @Transactional is there but no rollback on business errors.

**Evidence**:
```java
if (sourceAccount.getBalance().compareTo(total) < 0) {
    // BAD: Returns error but transaction still commits!
    Map<String, Object> resp = new HashMap<>();
    resp.put("status", "FAILED");
    return resp;  // Transaction committed, balance changed!
}
```

**Fix**: Level 6 - Throw exceptions for rollback

---

### 11. No Tests

**Problem**: Zero test coverage.

**Current Coverage**: 0%

**Missing Tests**:
- Unit tests for business logic
- Integration tests for API
- Edge case tests
- Error scenario tests

**Fix**: Level 7 - Write comprehensive tests

---

### 12. Unsafe Concurrent Access

**Problem**: No handling of concurrent transfers.

**Scenario**:
```
Account balance: ₹1000

Thread 1: Transfer ₹800
Thread 2: Transfer ₹800

Both check balance (₹1000) ✓
Both proceed to debit
Final balance: -₹600 💥 (Negative balance!)
```

**Missing**:
- Optimistic locking (@Version)
- Pessimistic locking
- Database constraints

**Fix**: Level 11 - Add concurrency handling

---

### 13. No Logging

**Problem**: No logs for debugging or audit.

**Missing**:
- Request/response logging
- Error logging
- Business event logging
- Audit trail

**Fix**: Level 6 - Add logging

---

### 14. No API Documentation

**Problem**: No Swagger/OpenAPI annotations.

**Missing**:
- Endpoint descriptions
- Request/response examples
- Error codes documentation

**Fix**: Level 2 OpenAPI spec should be implemented

---

### 15. Security Issues

**Problems**:
- No authentication (@RequestHeader JWT token)
- No authorization (user can transfer from any account!)
- No input sanitization (SQL injection risk)
- No rate limiting

**Evidence**:
```java
@PostMapping("/transfers")
public Map<String, Object> transfer(@RequestBody Map<String, Object> req) {
    // Anyone can call this!
    // Can transfer from any account!
}
```

**Fix**: Level 6 - Add security

---

## Metrics Baseline

### Cyclomatic Complexity
| Method | Complexity | Status |
|--------|-----------|--------|
| transfer() | 15 | ❌ High (>10) |
| getStatus() | 3 | ⚠️ Medium |
| initData() | 1 | ✅ Low |

**Target**: <10 per method

### Lines of Code
| Method | LOC | Status |
|--------|-----|--------|
| transfer() | 127 | ❌ Too long (>50) |
| getStatus() | 22 | ✅ OK |
| initData() | 45 | ⚠️ Acceptable |

**Target**: <50 lines per method

### Code Coverage
- **Unit Tests**: 0%
- **Integration Tests**: 0%
- **Total Coverage**: 0%

**Target**: >80%

### Code Smells (SonarQube)
- **Blocker**: 3
- **Critical**: 8
- **Major**: 15
- **Minor**: 22

**Target**: 0 blocker, 0 critical

---

## Summary of Bad Practices

| # | Practice | Severity | Fix In Level |
|---|----------|----------|--------------|
| 1 | God Object | 🔴 Critical | Level 6 |
| 2 | No Separation of Concerns | 🔴 Critical | Level 6 |
| 3 | Direct DB Access | 🔴 Critical | Level 6 |
| 4 | No Validation | 🔴 Critical | Level 6 |
| 5 | No Error Handling | 🔴 Critical | Level 6 |
| 6 | Magic Numbers | 🟡 Major | Level 6 |
| 7 | Poor Naming | 🟡 Major | Level 6 |
| 8 | Long Methods | 🟡 Major | Level 6 |
| 9 | Primitive Obsession | 🟡 Major | Level 6 |
| 10 | Transaction Management | 🟡 Major | Level 6 |
| 11 | No Tests | 🔴 Critical | Level 7 |
| 12 | Concurrency Issues | 🔴 Critical | Level 11 |
| 13 | No Logging | 🟡 Major | Level 6 |
| 14 | No Documentation | 🟢 Minor | Level 2 |
| 15 | Security Issues | 🔴 Critical | Level 19 |

**Total Issues**: 48+

---

## Learning Objectives

By seeing this bad code, participants will:

1. **Recognize Code Smells**: Identify bad practices in real code
2. **Understand Impact**: See how bad code makes testing impossible
3. **Appreciate SOLID**: Understand why principles matter
4. **Value Refactoring**: See the need for continuous improvement
5. **Motivation for TDD**: Understand why tests come first

---

## Next Steps

1. ✅ Run the application (it works!)
2. ✅ Try to test it (you can't!)
3. ✅ Run SonarQube (see the horror!)
4. ⏳ Level 5: Learn TDD
5. ⏳ Level 6: Refactor with SOLID
6. ⏳ Level 7: Write comprehensive tests
