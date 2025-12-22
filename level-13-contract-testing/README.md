# Level 13: Contract Testing - Provider Side

Contract-first API testing using Spring Cloud Contract to ensure API compatibility.

## What's New in Level 13

- **Spring Cloud Contract** - Provider-side contract testing
- **Contract-First Development** - Define API behavior in contracts BEFORE implementation
- **Auto-Generated Tests** - Tests generated from contract definitions
- **Consumer Stubs** - Publish stubs for consumer teams
- **API Versioning** - Ensure backward compatibility

## What is Contract Testing?

**Consumer-Provider Pattern**:
```
Consumer (Frontend/Mobile App)
    ↓ Expects specific API
Provider (This Service)
    ↓ Must fulfill contract
```

**Problem Without Contracts**:
- Provider changes API → Consumer breaks in production
- No way to verify compatibility before deployment
- Integration issues discovered late

**Solution With Contracts**:
- Contract defines expected API behavior
- Provider tests verify API matches contract
- Consumer uses stubs based on same contract
- Changes breaking contract detected early

## Contract Structure

### Example Contract (Groovy DSL)

```groovy
Contract.make {
    description "Should successfully initiate a transfer"
    
    request {
        method POST()
        url "/v1/transfers"
        headers {
            contentType(applicationJson())
        }
        body([
            sourceUPI: "alice@okaxis",
            destinationUPI: "bob@paytm",
            amount: 500.00
        ])
    }
    
    response {
        status OK()
        body([
            transactionId: anyNonEmptyString(),
            status: "SUCCESS",
            amount: 500.00
        ])
    }
}
```

## How It Works

```
1. Contracts defined (Groovy DSL files)
   ↓
2. Maven plugin generates tests
   ↓
3. Generated tests extend BaseContractTest
   ↓
4. Tests run against real implementation
   ↓
5. If pass → Generate stubs
   ↓
6. Publish stubs to repository
```

## Directory Structure

```
src/
├── main/java/          # Production code
├── test/
    ├── java/
    │   └── contract/
    │       └── BaseContractTest.java
    └── resources/
        └── contracts/
            └── transfers/
                ├── shouldSuccessfullyInitiateTransfer.groovy
                ├── shouldReturnBadRequestForInsufficientBalance.groovy
                ├── shouldReturnNotFoundForNonExistentAccount.groovy
                ├── shouldReturnBadRequestForNegativeAmount.groovy
                └── shouldReturnBadRequestForMissingFields.groovy
```

## Quick Start

### 1. Generate Tests from Contracts

```bash
mvn clean compile
```

### 2. Run All Tests

```bash
mvn test
```

**Expected**: 116 tests pass (111 previous + 5 new contract tests)

### 3. View Generated Tests

```bash
ls target/generated-test-sources/contracts/com/npci/transfer/contract/
```

### 4. Install Stubs

```bash
mvn clean install
```

Stubs location: `target/stubs/`

## Contracts Included

1. **Successful Transfer** - Happy path (200)
2. **Insufficient Balance** - Business error (400)
3. **Account Not Found** - Missing account (404)
4. **Negative Amount** - Validation error (400)
5. **Missing Fields** - Required field validation (400)

## Benefits

✅ Contract defines API behavior  
✅ Auto-generated tests  
✅ Consumer stubs for parallel development  
✅ Breaking changes detected early  
✅ Safe refactoring  
✅ Parallel team development  

## Test Coverage

**116 Total Tests**
- 75 unit tests
- 6 component tests
- 10 repository tests
- 10 REST-Assured integration tests
- **5 contract tests** (NEW!)

## Technology

- Spring Cloud Contract 4.1.0
- Groovy DSL for contracts
- JUnit 5
- Testcontainers PostgreSQL

---

**Contract-First** - Define before implementing  
**Auto-Generated** - Write contracts, get tests free  
**Parallel Development** - Teams don't block each other
