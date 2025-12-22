# Level 13: Contract Testing - Provider Side - Teaching Notes (Updated)

## Session Overview
- Introduction to contract testing concepts
- Spring Cloud Contract framework
- Provider-side testing with auto-generated tests
- Groovy DSL for contract definitions
- Consumer stubs generation
- Real-world troubleshooting and fixes
- Transaction context management
- Clean state strategies for different test types

---

## Part 1: The Problem - Why Contract Testing?

### Traditional Integration Testing Issues

**Scenario**: Transfer Service (Provider) + Mobile App (Consumer)

**Without Contracts**:
```
Day 1: Provider implements API
   POST /v1/transfers
   Response: {"transactionId": "TXN123", "status": "SUCCESS"}

Day 30: Provider changes response format
   Response: {"txnId": "TXN123", "state": "SUCCESS"}  // Changed field names!

Day 31: Mobile app breaks in production ❌
   - Mobile app expects "transactionId"
   - Mobile app expects "status"
   - Both fields renamed → App crashes
```

**Problems**:
1. ❌ No early warning system
2. ❌ Integration issues found in QA/production (expensive)
3. ❌ Provider and consumer tightly coupled
4. ❌ Can't develop in parallel (consumer waits for provider)
5. ❌ Refactoring is risky

**With Contract Testing**:
```
Day 1: Teams agree on contract
   Contract: POST /v1/transfers returns {"transactionId", "status"}

Day 15: Provider implements API
   Contract test verifies: ✅ API matches contract

Day 30: Provider tries to change response
   Contract test fails: ❌ Response doesn't match contract
   → Fix before deployment

Day 31: Mobile app works perfectly ✅
```

**Benefits**:
1. ✅ Early detection of breaking changes
2. ✅ Contract serves as API documentation
3. ✅ Provider and consumer develop in parallel
4. ✅ Safe refactoring (contract ensures compatibility)
5. ✅ Consumer uses stubs (doesn't need real provider)

---

## Part 2: Contract Testing Concepts

### Consumer-Driven Contracts (CDC)

**Definition**: 
Contract testing where **consumers** define what they need from the **provider**.

**Flow**:
```
1. Consumer Team: "We need POST /v1/transfers to return transaction ID"
   ↓
2. Teams agree on contract (API specification)
   ↓
3. Provider Team: Implements API + runs contract tests
   ↓
4. Contract tests verify: Implementation matches contract
   ↓
5. If pass: Generate stubs for consumers
   ↓
6. Consumers: Test against stubs (without real provider)
```

**Key Players**:

| Role | Responsibility | Tool |
|------|---------------|------|
| **Consumer** | Defines what they need | Uses stubs |
| **Provider** | Implements API | Runs contract tests |
| **Contract** | Specification both agree on | Groovy DSL file |

---

## Part 3: Spring Cloud Contract Framework

### What is Spring Cloud Contract?

**Components**:
1. **Contract Definitions** (Groovy DSL files)
2. **Maven Plugin** (Generates tests)
3. **Base Test Class** (Setup for tests)
4. **Generated Tests** (Run against real API)
5. **Stubs** (For consumers)

### Spring Cloud Contract Architecture

```
┌─────────────────────────────────────┐
│ contracts/transfers/*.groovy         │  ← Contract definitions
│ (What API should do)                 │
└──────────────┬──────────────────────┘
               │
               ↓ (Maven plugin reads)
┌──────────────────────────────────────┐
│ spring-cloud-contract-maven-plugin   │
│ (Code generation)                    │
└──────────────┬───────────────────────┘
               │
               ↓ (Generates)
┌──────────────────────────────────────┐
│ target/generated-test-sources/       │
│ ContractVerifierTest.java            │  ← Auto-generated tests
│ (JUnit 5 test class)                 │
└──────────────┬───────────────────────┘
               │
               ↓ (Extends)
┌──────────────────────────────────────┐
│ BaseContractTest.java                │  ← Your base class
│ (Setup: database, REST-Assured)      │
└──────────────┬───────────────────────┘
               │
               ↓ (Tests)
┌──────────────────────────────────────┐
│ Real Spring Boot Application         │
│ (Your actual API implementation)     │
└──────────────┬───────────────────────┘
               │
               ↓ (If pass)
┌──────────────────────────────────────┐
│ target/stubs/                        │
│ (Consumer stubs)                     │  ← For consumers
└──────────────────────────────────────┘
```

---

## Part 4: Groovy DSL Syntax Deep Dive

### Contract Structure

**Basic Template**:
```groovy
package contracts.transfers

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Human-readable description"
    
    request {
        // What HTTP request looks like
    }
    
    response {
        // What HTTP response should look like
    }
}
```

### Request Section

**Complete Example**:
```groovy
request {
    method POST()                    // HTTP method
    url "/v1/transfers"              // Endpoint
    headers {
        contentType(applicationJson())  // Content-Type: application/json
    }
    body([
        sourceUPI: "alice@okaxis",
        destinationUPI: "bob@paytm",
        amount: 500.00,
        remarks: "Payment"
    ])
}
```

### Response Section

**Complete Example**:
```groovy
response {
    status OK()                      // 200
    headers {
        contentType(applicationJson())
    }
    body([
        transactionId: anyNonEmptyString(),
        status: "SUCCESS",
        amount: 500.00
    ])
}
```

### Contract Matchers

**Consumer vs Producer Side**:
```groovy
transactionId: $(
    consumer(anyNonEmptyString()),              // Consumer: any string OK
    producer(regex('TXN-[0-9]{8,14}-[0-9]{4}')) // Provider: must match pattern
)
```

**Common Matchers**:
```groovy
// Any values
anyNonEmptyString()    // Any non-empty string
anyNumber()            // Any number
anyBoolean()           // true or false

// Patterns
regex('TXN-[0-9]{8}')  // Must match regex
regex('.*keyword.*')    // Contains "keyword"

// Equality
equalTo("SUCCESS")      // Exact match
```

**IMPORTANT**: Spring Cloud Contract DSL does NOT have:
- ❌ `containing()` - Use `regex('.*text.*')` instead
- ❌ `startsWith()` - Use `regex('^text.*')` instead
- ❌ `endsWith()` - Use `regex('.*text$')` instead

---

## Part 5: BaseContractTest Class - CRITICAL SETUP

### Purpose

**What It Does**:
- Base class for ALL generated contract tests
- Configures REST-Assured MockMvc
- Sets up test data (accounts)
- Extends PostgreSQLTestContainer (real database)

**Why Critical**:
- Generated tests need a base class
- Must provide clean state for each test
- Must NOT require transaction context (contract tests don't have it!)

### Code Breakdown

```java
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public abstract class BaseContractTest extends PostgreSQLTestContainer {
    
    @Autowired
    protected WebApplicationContext context;
    
    @Autowired
    protected AccountRepository accountRepository;
    
    @Autowired
    protected TransactionRepository transactionRepository;
    
    @BeforeEach
    public void setUp() {
        // 1. Configure REST-Assured MockMvc
        RestAssuredMockMvc.webAppContextSetup(context);
        
        // 2. Clean database
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        
        // 3. Create test accounts
        Account sourceAccount = Account.builder()
                .upiId("alice@okaxis")
                .balance(new BigDecimal("10000.00"))
                .build();
        accountRepository.save(sourceAccount);
        
        // Similar for destinationAccount and poorAccount
    }
}
```

**Key Annotations**:

1. **@SpringBootTest** - Full application context
2. **@ActiveProfiles("test")** - Use test configuration
3. **@DirtiesContext** - Clean context for each test method

**CRITICAL**: No `@Transactional`, No `EntityManager`!

---

## Part 6: CRITICAL LESSON - Transaction Context Issue

### The Problem We Encountered

**Error**:
```
TransfersTest>BaseContractTest.setUp:50 
» TransactionRequired No EntityManager with actual transaction available
```

**What Happened**:
```java
// WRONG - This doesn't work! ❌
@SpringBootTest
@Transactional  // Contract tests don't have transaction context!
public abstract class BaseContractTest {
    
    @PersistenceContext
    protected EntityManager entityManager;
    
    @BeforeEach
    public void setUp() {
        accountRepository.deleteAll();
        entityManager.flush();  // ERROR: No transaction!
    }
}
```

### Why It Failed

**Auto-Generated Contract Tests**:
- Run in MockMvc context (not full Spring)
- Do NOT have `@Transactional` annotation
- Cannot use EntityManager operations that require transactions
- `flush()` requires an active transaction

**Manual Component Tests**:
- Run in full Spring context
- CAN have `@Transactional` annotation
- CAN use EntityManager operations
- `flush()` works fine

### The Solution

**BaseContractTest** (For contract tests):
```java
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)  // ✅ Key!
public abstract class BaseContractTest {
    
    // ❌ NO @Transactional
    // ❌ NO EntityManager
    
    @BeforeEach
    public void setUp() {
        // Simple cleanup - works without transaction
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        
        // Create test data
        accountRepository.save(accounts);
    }
}
```

**TransferComponentTest** (For component tests):
```java
@SpringBootTest
@ActiveProfiles("test")
@Transactional  // ✅ Has transaction context
class TransferComponentTest {
    
    @PersistenceContext
    private EntityManager entityManager;  // ✅ Can use EntityManager
    
    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        entityManager.flush();  // ✅ Works with @Transactional
        entityManager.clear();
        
        // Create test data
        accountRepository.save(accounts);
        entityManager.flush();  // ✅ Works
    }
}
```

---

## Part 7: Clean State Strategies

### Three Approaches to Database Cleanup

**Approach 1: @DirtiesContext** (Contract tests)
```java
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@BeforeEach
public void setUp() {
    accountRepository.deleteAll();  // Simple!
}
```

**Pros**:
- ✅ Works without transaction
- ✅ Guaranteed clean state
- ✅ Works with MockMvc tests

**Cons**:
- ❌ Slower (recreates context)
- ❌ Higher memory usage

**When to Use**: Contract tests, tests without @Transactional

---

**Approach 2: EntityManager.flush()** (Component tests)
```java
@Transactional
@BeforeEach
void setUp() {
    accountRepository.deleteAll();
    entityManager.flush();  // Force delete
    entityManager.clear();  // Clear cache
    
    accountRepository.save(accounts);
    entityManager.flush();  // Force save
}
```

**Pros**:
- ✅ Faster (no context recreation)
- ✅ Precise control over persistence
- ✅ Lower memory usage

**Cons**:
- ❌ Requires @Transactional
- ❌ More complex
- ❌ Doesn't work with MockMvc

**When to Use**: Component tests with @Transactional

---

**Approach 3: @Sql Scripts**
```java
@Sql(scripts = "/cleanup.sql", executionPhase = BEFORE_TEST_METHOD)
@BeforeEach
void setUp() {
    // Database cleaned by SQL script
    accountRepository.save(accounts);
}
```

**Pros**:
- ✅ Database-level cleanup
- ✅ Works with any test type
- ✅ Can clean complex schemas

**Cons**:
- ❌ Extra SQL files to maintain
- ❌ Less flexible
- ❌ Database-specific

**When to Use**: Complex schemas, specific cleanup needs

---

## Part 8: Comparison Table

### Contract Tests vs Component Tests

| Aspect | Contract Tests | Component Tests |
|--------|---------------|-----------------|
| **Base Class** | BaseContractTest | TransferComponentTest |
| **Context** | MockMvc | Full Spring |
| **@Transactional** | ❌ No | ✅ Yes |
| **EntityManager** | ❌ Can't use | ✅ Can use |
| **Cleanup Strategy** | @DirtiesContext | EntityManager.flush() |
| **Speed** | Medium | Faster |
| **Test Generation** | Auto-generated | Manual |
| **Purpose** | Verify contract | Verify integration |

---

## Part 9: Generated Tests

### What Gets Generated

**From Contract**:
```
contracts/transfers/shouldSuccessfullyInitiateTransfer.groovy
```

**Generated Test**:
```
target/generated-test-sources/contracts/com/npci/transfer/contract/
  └── ContractVerifierTest.java
```

**Generated Test Content** (simplified):
```java
public class ContractVerifierTest extends BaseContractTest {
    
    @Test
    public void validate_shouldSuccessfullyInitiateTransfer() throws Exception {
        // Setup
        MockMvcRequestSpecification request = given();
        
        // Execute
        Response response = given().spec(request)
            .header("Content-Type", "application/json")
            .body("{\"sourceUPI\":\"alice@okaxis\",...}")
        .when()
            .post("/v1/transfers");
        
        // Verify
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.header("Content-Type")).matches("application/json.*");
        
        // ... more assertions from contract
    }
}
```

**You Never Write This!** - Spring Cloud Contract generates it automatically.

---

## Part 10: Real-World Bug Fixes We Made

### Bug #1: Duplicate Key Errors

**Symptom**:
```
ERROR: duplicate key value violates unique constraint "accounts_upi_id_key"
Detail: Key (upi_id)=(alice@okaxis) already exists.
```

**Root Cause**:
- Singleton Testcontainers keeps ONE container alive
- Data persists across test classes
- BaseContractTest creates `alice@okaxis`
- TransferComponentTest tries to create `alice@okaxis` again
- BOOM! Duplicate key error

**Fix**:
```java
// TransferComponentTest (not BaseContractTest!)
@Transactional
@BeforeEach
void setUp() {
    accountRepository.deleteAll();
    entityManager.flush();  // ✅ Force delete to complete
    entityManager.clear();  // ✅ Clear cache
    
    // Now safe to create accounts
    accountRepository.save(accounts);
    entityManager.flush();  // ✅ Force save to complete
}
```

**Lesson**: With singleton Testcontainers, use EntityManager.flush() in @Transactional tests

---

### Bug #2: Wrong Contract Expectations

**Symptom**:
```
TransfersTest.validate_shouldReturnBadRequestForNegativeAmount:87 
» IllegalState Parsed JSON doesn't match
```

**Root Cause**:
Contract expected: `"Amount must be greater than 0.01"`  
Actual application returns: `"Minimum transfer amount is ₹1"`

**Fix**:
```groovy
// BEFORE (Wrong) ❌
errors: ["Amount must be greater than 0.01"]

// AFTER (Fixed) ✅
errors: $(consumer(anyNonEmptyString()), 
          producer(regex('.*[Mm]inimum.*')))
```

**Lesson**: Always test actual application FIRST, then write contracts to match

---

### Bug #3: Transaction Context Error

**Symptom**:
```
TransfersTest>BaseContractTest.setUp:50 
» TransactionRequired No EntityManager with actual transaction available
```

**Root Cause**:
BaseContractTest used `entityManager.flush()` but contract tests don't have @Transactional

**Fix**:
```java
// BEFORE (Wrong) ❌
@Transactional
@BeforeEach
public void setUp() {
    entityManager.flush();  // No transaction context!
}

// AFTER (Fixed) ✅
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@BeforeEach
public void setUp() {
    accountRepository.deleteAll();  // Works without flush!
}
```

**Lesson**: Contract tests use @DirtiesContext, NOT @Transactional

---

## Part 11: Best Practices from Real Experience

### DO ✅

1. **Test actual application FIRST before writing contracts**
   ```bash
   # Run application
   mvn spring-boot:run
   
   # Test with curl
   curl -X POST http://localhost:8080/v1/transfers \
     -H "Content-Type: application/json" \
     -d '{"sourceUPI":"alice@okaxis",...}'
   
   # Note actual response
   # THEN write contract to match
   ```

2. **Use flexible matchers for dynamic fields**
   ```groovy
   // Good ✅
   transactionId: regex('TXN-[0-9]{8,14}-[0-9]{4}')
   
   // Too strict ❌
   transactionId: regex('TXN-[0-9]{8}-[0-9]{4}')
   ```

3. **Use @DirtiesContext for contract tests**
   ```java
   @DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
   public abstract class BaseContractTest {
       // No @Transactional, no EntityManager
   }
   ```

4. **Use EntityManager.flush() only in @Transactional tests**
   ```java
   @Transactional
   class ComponentTest {
       entityManager.flush();  // ✅ OK here
   }
   ```

5. **Keep contracts in separate directory per feature**
   ```
   contracts/
     ├── transfers/
     ├── accounts/
     └── reports/
   ```

### DON'T ❌

1. **Don't use EntityManager in BaseContractTest**
   ```java
   // Wrong ❌
   public abstract class BaseContractTest {
       @PersistenceContext
       EntityManager entityManager;  // Contract tests can't use this!
   }
   ```

2. **Don't use containing() in contracts**
   ```groovy
   // Wrong ❌
   message: containing("error")
   
   // Right ✅
   message: regex('.*error.*')
   ```

3. **Don't guess contract responses**
   ```groovy
   // Wrong ❌ - guessing
   errors: ["Amount must be positive"]
   
   // Right ✅ - tested actual app
   errors: regex('.*[Mm]inimum.*')
   ```

4. **Don't make contracts too strict**
   ```groovy
   // Too strict ❌
   timestamp: "2025-12-22T10:30:45.123"
   
   // Flexible ✅
   timestamp: regex('[0-9]{4}-[0-9]{2}-[0-9]{2}T.*')
   ```

---

## Part 12: Maven Plugin Configuration

### pom.xml Setup

```xml
<properties>
    <spring-cloud-contract.version>4.1.0</spring-cloud-contract.version>
</properties>

<dependencies>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-contract-verifier</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-contract-dependencies</artifactId>
            <version>${spring-cloud-contract.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-contract-maven-plugin</artifactId>
            <version>${spring-cloud-contract.version}</version>
            <extensions>true</extensions>
            <configuration>
                <testFramework>JUNIT5</testFramework>
                <baseClassForTests>com.npci.transfer.contract.BaseContractTest</baseClassForTests>
                <contractsDirectory>${project.basedir}/src/test/resources/contracts</contractsDirectory>
            </configuration>
        </plugin>
    </plugins>
</build>
```

---

## Part 13: Build Process

### Step-by-Step Execution

```bash
# Step 1: Generate tests from contracts
mvn clean compile
```

**What happens**:
1. Maven reads pom.xml
2. spring-cloud-contract-maven-plugin activates
3. Plugin reads contracts from `src/test/resources/contracts/`
4. For each `.groovy` file, generate a test method
5. All test methods in one `ContractVerifierTest.java`
6. Tests saved to `target/generated-test-sources/`

```bash
# Step 2: Run all tests
mvn test
```

**What happens**:
1. Compile generated tests
2. Run all tests (unit + component + integration + contract)
3. Contract tests execute against real API
4. Verify API matches contracts
5. If pass: Generate stubs in `target/stubs/`

```bash
# Step 3: Install stubs
mvn clean install
```

**What happens**:
1. Package application as JAR
2. Package stubs as ZIP
3. Install to local Maven repository
4. Consumers can now download stubs

---

## Part 14: Consumer Stubs

### How Consumers Use Stubs

**Provider publishes** (us):
```bash
mvn deploy  # Publishes to Maven repository
```

**Consumer downloads**:
```bash
# In consumer's pom.xml
<dependency>
    <groupId>com.npci</groupId>
    <artifactId>transfer-service-contract</artifactId>
    <version>7.0.0</version>
    <classifier>stubs</classifier>
</dependency>
```

**Consumer tests against stubs**:
```java
@AutoConfigureStubRunner(
    ids = "com.npci:transfer-service-contract:+:stubs:8080",
    stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
public class MobileAppTest {
    
    @Test
    public void shouldInitiateTransfer() {
        // Call stub (not real service!)
        Response response = given()
            .body("{\"sourceUPI\":\"alice@okaxis\",...}")
        .when()
            .post("http://localhost:8080/v1/transfers");
        
        // Stub returns contract response
        assertEquals("SUCCESS", response.path("status"));
    }
}
```

**Benefits**:
- ✅ Consumer doesn't need real provider
- ✅ Fast tests (no network)
- ✅ Develop in parallel
- ✅ Integration works first time

---

## Part 15: Key Takeaways

### Technical Concepts Mastered

1. **Contract Testing**
   - Consumer-Driven Contracts (CDC)
   - Provider-side verification
   - Consumer stubs generation

2. **Spring Cloud Contract**
   - Groovy DSL syntax
   - Auto-generated tests
   - Maven plugin configuration

3. **Transaction Context Management**
   - When to use @Transactional
   - When to use @DirtiesContext
   - EntityManager.flush() usage

4. **Clean State Strategies**
   - @DirtiesContext for contract tests
   - EntityManager for component tests
   - When to use each approach

5. **Real-World Troubleshooting**
   - Duplicate key errors with singleton Testcontainers
   - Contract expectation mismatches
   - Transaction context issues

### Common Pitfalls to Avoid

1. ❌ **Using EntityManager in BaseContractTest** → Use @DirtiesContext instead
2. ❌ **Using containing() in contracts** → Use regex() instead
3. ❌ **Guessing contract responses** → Test actual app first
4. ❌ **Too strict matchers** → Use flexible patterns
5. ❌ **No cleanup between tests** → Use proper cleanup strategy
6. ❌ **Mixing test strategies** → Contract tests ≠ Component tests

### Production-Ready Checklist

- ✅ All 116 tests passing
- ✅ Contracts match actual application
- ✅ BaseContractTest uses @DirtiesContext
- ✅ Component tests use EntityManager.flush()
- ✅ Singleton Testcontainers working
- ✅ Stubs generated successfully
- ✅ No duplicate key errors
- ✅ No transaction context errors

---

## Summary

**Level 13 Achievements**:
- ✅ Learned contract testing concepts
- ✅ Implemented Spring Cloud Contract
- ✅ Created 5 contract definitions
- ✅ Auto-generated 5 contract tests
- ✅ Generated consumer stubs
- ✅ Fixed real-world issues:
  - Duplicate key errors
  - Contract expectation mismatches
  - Transaction context problems
- ✅ Learned clean state strategies
- ✅ Production-ready implementation

**Next Steps**:
- Level 14: Consumer-side contract testing
- Level 15: Advanced contract patterns
- Level 16: Contract testing in CI/CD

**Real-World Impact**:
- Prevents breaking changes in production
- Enables parallel team development
- Reduces integration issues
- Provides living API documentation
- Supports safe refactoring
- Improves team collaboration

---

## Questions for Students

1. What's the difference between contract tests and component tests?
2. Why can't we use EntityManager in BaseContractTest?
3. When should you use @DirtiesContext vs @Transactional?
4. Why don't Spring Cloud Contract DSL have `containing()`?
5. How do you fix duplicate key errors with singleton Testcontainers?
6. What's the benefit of auto-generated tests?
7. How do consumers use provider stubs?
8. When should contracts be flexible vs strict?
9. What's the proper order: code first or contracts first?
10. How do contract tests enable parallel development?

---

## Hands-On Exercises

**Exercise 1: Add New Contract**
- Create contract for GET /v1/transfers/{id}
- Test actual application first
- Write contract to match
- Verify auto-generation

**Exercise 2: Fix a Transaction Error**
- Add EntityManager.flush() to BaseContractTest
- Run tests and observe error
- Fix with @DirtiesContext
- Understand why it happened

**Exercise 3: Create Flexible Contract**
- Write contract with too-strict matcher
- Run test and see failure
- Fix with flexible regex
- Compare both approaches

**Exercise 4: Test Duplicate Key Issue**
- Comment out entityManager.flush() in TransferComponentTest
- Run tests and see duplicate key error
- Add flush() back
- Understand singleton Testcontainers behavior

**Exercise 5: Generate and Use Stubs**
- Run mvn install
- Explore target/stubs/
- Understand stub JSON structure
- Imagine how consumer would use it

---

**End of Level 13 Teaching Notes (Updated with Real-World Fixes)**