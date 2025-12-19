# SOLID Principles Guide

## Overview

SOLID is an acronym for five design principles that make software more maintainable, flexible, and scalable.

**Created by**: Robert C. Martin (Uncle Bob)

---

## S - Single Responsibility Principle (SRP)

### Definition
**A class should have only one reason to change.**

Each class should focus on doing one thing well.

---

### Problem: God Object (Level 4)

```java
@RestController
public class TransferController {
    
    @PersistenceContext
    private EntityManager em;
    
    public Map<String, Object> transfer(@RequestBody Map<String, Object> req) {
        // 1. HTTP handling
        String src = (String) req.get("sourceUPI");
        
        // 2. Validation
        if (src == null) { ... }
        
        // 3. Database access
        List<Account> accounts = em.createQuery(...).getResultList();
        
        // 4. Business logic
        BigDecimal fee = amount > 1000 ? 5.0 : 0.0;
        
        // 5. Transaction management
        sourceAccount.setBalance(balance.subtract(total));
        
        // 6. Response construction
        Map<String, Object> response = new HashMap<>();
        
        return response;
    }
}
```

**Problems**:
- Changes to validation require touching controller
- Changes to business logic require touching controller
- Changes to database require touching controller
- **6 reasons to change!** ❌

---

### Solution: Separate Responsibilities

```java
// 1. Controller - HTTP only (ONE responsibility)
@RestController
public class TransferController {
    
    private final TransferService transferService;
    
    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }
    
    @PostMapping("/v1/transfers")
    public ResponseEntity<TransferResponse> transfer(
            @Valid @RequestBody TransferRequest request) {
        
        TransferResponse response = transferService.initiateTransfer(request);
        return ResponseEntity.ok(response);
    }
}

// 2. Service - Business logic only (ONE responsibility)
@Service
public class TransferService {
    
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransferValidator validator;
    private final FeeCalculator feeCalculator;
    
    public TransferResponse initiateTransfer(TransferRequest request) {
        validator.validate(request);
        
        Account source = accountRepository.findByUpiId(request.getSourceUPI())
            .orElseThrow(() -> new AccountNotFoundException("Source account not found"));
        
        Account destination = accountRepository.findByUpiId(request.getDestinationUPI())
            .orElseThrow(() -> new AccountNotFoundException("Destination account not found"));
        
        BigDecimal fee = feeCalculator.calculateFee(request.getAmount());
        BigDecimal total = request.getAmount().add(fee);
        
        // Business logic
        source.debit(total);
        destination.credit(request.getAmount());
        
        accountRepository.save(source);
        accountRepository.save(destination);
        
        Transaction transaction = createTransaction(request, fee, total);
        transactionRepository.save(transaction);
        
        return buildResponse(transaction);
    }
}

// 3. Repository - Data access only (ONE responsibility)
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByUpiId(String upiId);
}

// 4. Validator - Validation only (ONE responsibility)
@Component
public class TransferValidator {
    public void validate(TransferRequest request) {
        // Validation logic
    }
}

// 5. FeeCalculator - Fee calculation only (ONE responsibility)
@Component
public class FeeCalculator {
    public BigDecimal calculateFee(BigDecimal amount) {
        // Fee logic
    }
}
```

**Benefits**:
- Controller: ONE reason to change (HTTP API changes)
- Service: ONE reason to change (business rules)
- Repository: ONE reason to change (data access)
- Validator: ONE reason to change (validation rules)
- FeeCalculator: ONE reason to change (fee rules)

✅ **Each class has single responsibility!**

---

## O - Open/Closed Principle (OCP)

### Definition
**Software entities should be open for extension but closed for modification.**

You should be able to add new functionality without changing existing code.

---

### Problem: Hardcoded Logic

```java
public class TransferService {
    
    public BigDecimal calculateFee(BigDecimal amount) {
        // BAD: To add new fee rules, must modify this method
        if (amount.compareTo(new BigDecimal("1000")) > 0) {
            return new BigDecimal("5.00");
        }
        return BigDecimal.ZERO;
    }
}
```

**What if requirements change?**
- Premium accounts: No fee
- Weekend transfers: Higher fee
- International transfers: Different fee

**You'd have to modify calculateFee() every time!** ❌

---

### Solution: Strategy Pattern

```java
// 1. Define interface (abstraction)
public interface FeeCalculationStrategy {
    BigDecimal calculateFee(BigDecimal amount, Account source, Account destination);
}

// 2. Concrete strategies (EXTENSIONS, not modifications)
@Component
public class StandardFeeStrategy implements FeeCalculationStrategy {
    
    private static final BigDecimal THRESHOLD = new BigDecimal("1000");
    private static final BigDecimal FEE = new BigDecimal("5.00");
    
    @Override
    public BigDecimal calculateFee(BigDecimal amount, Account source, Account destination) {
        return amount.compareTo(THRESHOLD) > 0 ? FEE : BigDecimal.ZERO;
    }
}

@Component
public class PremiumFeeStrategy implements FeeCalculationStrategy {
    
    @Override
    public BigDecimal calculateFee(BigDecimal amount, Account source, Account destination) {
        // Premium accounts pay no fee
        return BigDecimal.ZERO;
    }
}

@Component
public class WeekendFeeStrategy implements FeeCalculationStrategy {
    
    @Override
    public BigDecimal calculateFee(BigDecimal amount, Account source, Account destination) {
        // Higher fee on weekends
        return amount.compareTo(THRESHOLD) > 0 
            ? new BigDecimal("10.00") 
            : new BigDecimal("2.00");
    }
}

// 3. Context that uses strategy
@Service
public class FeeCalculator {
    
    private final Map<String, FeeCalculationStrategy> strategies;
    
    public FeeCalculator(List<FeeCalculationStrategy> strategyList) {
        this.strategies = strategyList.stream()
            .collect(Collectors.toMap(
                s -> s.getClass().getSimpleName(),
                Function.identity()
            ));
    }
    
    public BigDecimal calculateFee(BigDecimal amount, Account source, Account destination) {
        // Select strategy based on account type, day, etc.
        FeeCalculationStrategy strategy = selectStrategy(source);
        return strategy.calculateFee(amount, source, destination);
    }
    
    private FeeCalculationStrategy selectStrategy(Account source) {
        if (source.isPremium()) {
            return strategies.get("PremiumFeeStrategy");
        }
        if (isWeekend()) {
            return strategies.get("WeekendFeeStrategy");
        }
        return strategies.get("StandardFeeStrategy");
    }
}
```

**Benefits**:
- Add new fee strategies without modifying existing code
- Each strategy is testable independently
- Easy to switch strategies at runtime

✅ **Open for extension, closed for modification!**

---

## L - Liskov Substitution Principle (LSP)

### Definition
**Objects of a superclass should be replaceable with objects of a subclass without breaking the application.**

Subtypes must be substitutable for their base types.

---

### Problem: Violated Substitution

```java
public class Account {
    public void debit(BigDecimal amount) {
        this.balance = this.balance.subtract(amount);
    }
}

public class SavingsAccount extends Account {
    @Override
    public void debit(BigDecimal amount) {
        // BAD: Changes behavior!
        if (amount.compareTo(new BigDecimal("10000")) > 0) {
            throw new IllegalArgumentException("Cannot debit more than ₹10,000 from savings");
        }
        super.debit(amount);
    }
}

// Usage
public void transferMoney(Account source, Account destination, BigDecimal amount) {
    source.debit(amount);  // Works for Account
    // But throws exception for SavingsAccount! ❌
    // Violates LSP!
}
```

---

### Solution: Consistent Behavior

```java
public interface AccountRepository {
    Optional<Account> findByUpiId(String upiId);
    Account save(Account account);
    void delete(Account account);
}

// Implementation 1: JPA Repository
@Repository
public class JpaAccountRepository implements AccountRepository {
    
    @PersistenceContext
    private EntityManager em;
    
    @Override
    public Optional<Account> findByUpiId(String upiId) {
        List<Account> accounts = em.createQuery(
            "SELECT a FROM Account a WHERE a.upiId = :upi", Account.class)
            .setParameter("upi", upiId)
            .getResultList();
        return accounts.isEmpty() ? Optional.empty() : Optional.of(accounts.get(0));
    }
    
    @Override
    public Account save(Account account) {
        return em.merge(account);
    }
    
    @Override
    public void delete(Account account) {
        em.remove(account);
    }
}

// Implementation 2: In-Memory Repository (for testing)
public class InMemoryAccountRepository implements AccountRepository {
    
    private final Map<String, Account> accounts = new ConcurrentHashMap<>();
    
    @Override
    public Optional<Account> findByUpiId(String upiId) {
        return Optional.ofNullable(accounts.get(upiId));
    }
    
    @Override
    public Account save(Account account) {
        accounts.put(account.getUpiId(), account);
        return account;
    }
    
    @Override
    public void delete(Account account) {
        accounts.remove(account.getUpiId());
    }
}

// Usage - Works with BOTH implementations!
@Service
public class TransferService {
    
    private final AccountRepository accountRepository;  // Interface
    
    public TransferService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }
    
    public void transfer(TransferRequest request) {
        // Works with JpaAccountRepository
        // Works with InMemoryAccountRepository
        // Works with ANY AccountRepository implementation!
        Account source = accountRepository.findByUpiId(request.getSourceUPI())
            .orElseThrow();
    }
}
```

**Benefits**:
- Can swap JpaAccountRepository with InMemoryAccountRepository for tests
- Both implementations have same behavior (contract)
- No surprises!

✅ **Subtypes are perfectly substitutable!**

---

## I - Interface Segregation Principle (ISP)

### Definition
**Clients should not be forced to depend on interfaces they don't use.**

Many specific interfaces are better than one general-purpose interface.

---

### Problem: Fat Interface

```java
// BAD: Fat interface with too many methods
public interface AccountOperations {
    Account findByUpiId(String upiId);
    Account save(Account account);
    void delete(Account account);
    List<Account> findAll();
    List<Account> findByStatus(String status);
    void updateBalance(String upiId, BigDecimal balance);
    void updateDailyLimit(String upiId, BigDecimal limit);
    void updateMonthlyLimit(String upiId, BigDecimal limit);
    void resetDailyUsage(String upiId);
    void resetMonthlyUsage(String upiId);
    List<Account> findInactive();
    void sendNotification(String upiId, String message);
    // 20 more methods...
}

// TransferService only needs 2 methods but must depend on ALL 20+!
public class TransferService {
    private final AccountOperations accountOps;  // Too much!
}
```

**Problems**:
- TransferService depends on methods it doesn't use
- Changes to unused methods force recompilation
- Harder to understand what's actually needed

---

### Solution: Segregated Interfaces

```java
// 1. Basic CRUD operations
public interface AccountRepository {
    Optional<Account> findByUpiId(String upiId);
    Account save(Account account);
    void delete(Account account);
}

// 2. Query operations (separate interface)
public interface AccountQueryService {
    List<Account> findAll();
    List<Account> findByStatus(String status);
    List<Account> findInactive();
}

// 3. Limit management (separate interface)
public interface AccountLimitService {
    void updateDailyLimit(String upiId, BigDecimal limit);
    void updateMonthlyLimit(String upiId, BigDecimal limit);
    void resetDailyUsage(String upiId);
    void resetMonthlyUsage(String upiId);
}

// 4. Notification service (separate interface)
public interface AccountNotificationService {
    void sendNotification(String upiId, String message);
}

// Now clients depend only on what they need!
@Service
public class TransferService {
    
    private final AccountRepository accountRepository;  // Only CRUD
    
    public TransferService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }
}

@Service
public class AccountReportService {
    
    private final AccountQueryService queryService;  // Only queries
    
    public AccountReportService(AccountQueryService queryService) {
        this.queryService = queryService;
    }
}

@Service
public class AccountLimitManager {
    
    private final AccountLimitService limitService;  // Only limits
    
    public AccountLimitManager(AccountLimitService limitService) {
        this.limitService = limitService;
    }
}
```

**Benefits**:
- Each service depends only on what it needs
- Changes to unused interfaces don't affect clients
- Clear understanding of dependencies
- Easier to test (fewer mocks needed)

✅ **Focused, client-specific interfaces!**

---

## D - Dependency Inversion Principle (DIP)

### Definition
**High-level modules should not depend on low-level modules. Both should depend on abstractions.**

**Abstractions should not depend on details. Details should depend on abstractions.**

---

### Problem: Direct Dependencies (Level 4)

```java
// BAD: Service directly depends on concrete JPA implementation
@Service
public class TransferService {
    
    @PersistenceContext
    private EntityManager em;  // Direct dependency on JPA!
    
    public void transfer(TransferRequest request) {
        // Direct SQL/JPQL queries
        List<Account> accounts = em.createQuery(
            "SELECT a FROM Account a WHERE a.upiId = :upi", Account.class)
            .setParameter("upi", request.getSourceUPI())
            .getResultList();
        
        // Cannot test without real database!
        // Cannot switch to MongoDB!
        // Tightly coupled to JPA!
    }
}
```

**Problems**:
- Cannot test without database
- Cannot switch persistence technology
- Tight coupling to JPA
- High-level business logic depends on low-level database details

**Dependency Flow**:
```
TransferService (HIGH-LEVEL)
    ↓ depends on
EntityManager (LOW-LEVEL) ❌
```

---

### Solution: Depend on Abstractions

```java
// 1. Define abstraction (interface)
public interface AccountRepository {
    Optional<Account> findByUpiId(String upiId);
    Account save(Account account);
}

// 2. High-level module depends on abstraction
@Service
public class TransferService {
    
    private final AccountRepository accountRepository;  // Abstraction!
    
    // Constructor injection
    public TransferService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }
    
    public TransferResponse transfer(TransferRequest request) {
        // Uses interface, not concrete implementation
        Account source = accountRepository.findByUpiId(request.getSourceUPI())
            .orElseThrow();
        
        Account destination = accountRepository.findByUpiId(request.getDestinationUPI())
            .orElseThrow();
        
        // Business logic...
        
        accountRepository.save(source);
        accountRepository.save(destination);
        
        return buildResponse();
    }
}

// 3. Low-level module implements abstraction
@Repository
public class JpaAccountRepository implements AccountRepository {
    
    @PersistenceContext
    private EntityManager em;
    
    @Override
    public Optional<Account> findByUpiId(String upiId) {
        // JPA implementation details
    }
    
    @Override
    public Account save(Account account) {
        return em.merge(account);
    }
}

// 4. Alternative implementation (for testing)
public class InMemoryAccountRepository implements AccountRepository {
    
    private final Map<String, Account> accounts = new HashMap<>();
    
    @Override
    public Optional<Account> findByUpiId(String upiId) {
        return Optional.ofNullable(accounts.get(upiId));
    }
    
    @Override
    public Account save(Account account) {
        accounts.put(account.getUpiId(), account);
        return account;
    }
}
```

**Dependency Flow**:
```
                AccountRepository (ABSTRACTION)
                        ↑            ↑
                        |            |
                depends |            | depends
                        |            |
    TransferService (HIGH)      JpaAccountRepository (LOW)
```

Both HIGH and LOW depend on ABSTRACTION! ✅

**Benefits**:
- TransferService testable with InMemoryAccountRepository
- Can switch from JPA to MongoDB without changing TransferService
- Business logic independent of database technology
- Easy to mock for testing

```java
@Test
void shouldTransferSuccessfully() {
    // Use in-memory repository for testing!
    AccountRepository repo = new InMemoryAccountRepository();
    TransferService service = new TransferService(repo);
    
    // Setup test data
    Account source = new Account("alice@okaxis", new BigDecimal("10000"));
    Account dest = new Account("bob@paytm", new BigDecimal("5000"));
    repo.save(source);
    repo.save(dest);
    
    // Test
    TransferRequest request = new TransferRequest("alice@okaxis", "bob@paytm", 
        new BigDecimal("500"), "Test");
    service.transfer(request);
    
    // Verify - no database needed!
    Account updatedSource = repo.findByUpiId("alice@okaxis").get();
    assertThat(updatedSource.getBalance()).isEqualTo(new BigDecimal("9500"));
}
```

✅ **Depend on abstractions, not concretions!**

---

## Summary: SOLID Benefits

### S - Single Responsibility
✅ Each class has one job  
✅ Easier to understand  
✅ Easier to test  
✅ Fewer reasons to change  

### O - Open/Closed
✅ Add features without modifying existing code  
✅ Reduced risk of breaking working code  
✅ Strategy pattern for flexibility  

### L - Liskov Substitution
✅ Implementations are interchangeable  
✅ No surprises when swapping implementations  
✅ Consistent behavior  

### I - Interface Segregation
✅ Focused interfaces  
✅ Clients depend only on what they use  
✅ Easier to understand dependencies  

### D - Dependency Inversion
✅ Testable without real dependencies  
✅ Flexible to swap implementations  
✅ Business logic independent of infrastructure  

---

## SOLID in Action: Before vs After

### Before (Level 4)
```
❌ S: Controller does everything
❌ O: Must modify code for new features
❌ L: No abstractions to substitute
❌ I: No interfaces, only concrete classes
❌ D: Direct EntityManager dependency
```

### After (Level 6)
```
✅ S: Separate Controller, Service, Repository
✅ O: Strategy pattern for fees
✅ L: Repository implementations substitutable
✅ I: Focused interfaces (AccountRepository, TransactionRepository)
✅ D: Service depends on Repository interface
```

---

## Next Steps

1. ✅ Understand SOLID principles
2. ⏳ Apply to Level 4 bad code
3. ⏳ Refactor step by step
4. ⏳ Write tests for each layer
5. ⏳ Measure improvements
