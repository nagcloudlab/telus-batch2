# Level 13: Quick Start Guide

## Run All Tests

```bash
mvn clean test
```

**Expected Output**:
```
Tests run: 116, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## What's New

**Contract Testing** = Define API behavior in contracts, get tests for free!

### Contracts → Auto-Generated Tests

```
contracts/transfers/*.groovy  →  Generated test classes
                               →  Run against real API
                               →  Verify contract match
```

## Test Breakdown

| Test Type | Count | What's New |
|-----------|-------|------------|
| Unit Tests | 75 | - |
| Component Tests | 6 | - |
| Repository Tests | 10 | - |
| Integration Tests | 10 | - |
| **Contract Tests** | **5** | **✨ NEW!** |
| **TOTAL** | **116** | ✅ |

## New Contract Tests

1. ✅ Successful transfer (200)
2. ✅ Insufficient balance (400)
3. ✅ Account not found (404)
4. ✅ Negative amount validation (400)
5. ✅ Missing fields validation (400)

## View Generated Tests

```bash
# Generate tests
mvn clean compile

# View generated test files
ls target/generated-test-sources/contracts/com/npci/transfer/contract/
```

**Output**:
```
ContractVerifierTest.java
```

This file contains all 5 contract tests auto-generated from `.groovy` contracts!

## How It Works

### 1. Define Contract (contracts/*.groovy)

```groovy
Contract.make {
    request {
        method POST()
        url "/v1/transfers"
        body([sourceUPI: "alice@okaxis", ...])
    }
    response {
        status OK()
        body([status: "SUCCESS", ...])
    }
}
```

### 2. Maven Generates Test

```java
@Test
public void validate_shouldSuccessfullyInitiateTransfer() {
    // Auto-generated from contract
    // Tests your real API
}
```

### 3. Test Runs Against Real API

- Uses BaseContractTest (real database)
- Verifies API matches contract
- If pass → Contract fulfilled ✅

## Install & Publish Stubs

```bash
mvn clean install
```

**Stubs location**: `target/stubs/META-INF/com.npci/transfer-service-contract/7.0.0/mappings/`

**Consumer teams** use these stubs to test without your real service!

## Why Contract Testing?

### Without Contracts

```
Provider changes API → Consumer breaks in production ❌
```

### With Contracts

```
Provider changes API → Contract test fails → Fix before deploy ✅
```

## Key Files

- `pom.xml` - Spring Cloud Contract plugin
- `src/test/resources/contracts/**/*.groovy` - Contract definitions
- `src/test/java/contract/BaseContractTest.java` - Base class
- `target/generated-test-sources/` - Generated tests
- `target/stubs/` - Consumer stubs

## Benefits

✅ **Auto-generated tests** - Write contracts, not tests  
✅ **Early detection** - Breaking changes caught immediately  
✅ **Parallel development** - Consumer uses stubs while provider develops  
✅ **Safe refactoring** - Contract ensures compatibility  

---

**Ready to use!** Run `mvn test` to see all 116 tests pass! 🎯
