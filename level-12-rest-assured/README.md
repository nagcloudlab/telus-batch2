# Level 12: REST-Assured Integration Testing

Integration testing for REST APIs using REST-Assured framework.

## What's New in Level 12

- REST-Assured framework for API testing
- 10 comprehensive integration tests
- Full HTTP request/response testing
- Tests entire stack: Controller → Service → Repository → Database
- **Singleton Testcontainers** - ONE container for all tests (faster, no port conflicts)

## Quick Start

```bash
mvn clean test
```

**Expected**: All 101 tests pass ✅

## Integration Tests

All tests use real HTTP calls to test the complete application:

1. **Successful Transfer** - Happy path validation
2. **Insufficient Balance** - Business rule validation
3. **Non-existent Source** - 404 error handling
4. **Non-existent Destination** - 404 error handling
5. **Zero Amount** - Validation error
6. **Negative Amount** - Validation error
7. **Invalid UPI Format** - Format validation
8. **Missing Fields** - Required field validation
9. **Excessive Amount** - Limit validation
10. **Multiple Transfers** - Sequential operations

## Test Structure

```
src/test/java/com/npci/transfer/
├── config/
│   └── PostgreSQLTestContainer.java    # Singleton container (FIXED!)
├── integration/
│   ├── BaseIntegrationTest.java        # REST-Assured base class
│   └── TransferApiIntegrationTest.java # 10 API tests
├── component/                           # Component tests
├── repository/                          # Repository tests
└── service/                             # Service & controller tests
```

## REST-Assured Pattern

```java
given()
    .contentType(ContentType.JSON)
    .body("{...}")
.when()
    .post("/transfers")
.then()
    .statusCode(200)
    .body("status", equalTo("SUCCESS"));
```

## Test Coverage

- **101 Total Tests** (All Pass!)
  - 75 unit tests
  - 6 component tests (PostgreSQL)
  - 10 repository tests
  - 10 integration tests (NEW!)

## Key Features

✅ Real HTTP server on random port  
✅ **Singleton Testcontainers** - ONE container for ALL tests  
✅ Complete API validation  
✅ JSON response assertions  
✅ Status code verification  
✅ Error message validation  
✅ No port conflicts  
✅ Fast test execution  

## API Endpoint

```
POST /v1/transfers
Content-Type: application/json

{
  "sourceUPI": "alice@okaxis",
  "destinationUPI": "bob@paytm",
  "amount": 500.00,
  "remarks": "Payment"
}
```

## Critical Fix Applied

### Problem (Your Error)
- Running `mvn test` failed with connection errors
- Container shut down between test classes
- Error: `Connection to localhost:55492 refused`

### Solution (Applied)
- **Singleton Testcontainers pattern**
- ONE container starts at beginning
- ALL test classes share same container
- Container stops only after all tests complete

### Before vs After

**Before** (Per-Class Container):
```java
@Testcontainers  // New container PER test class
public abstract class PostgreSQLTestContainer {
    @Container
    static PostgreSQLContainer<?> postgres = ...;
}
```
❌ Container restarts between classes → Connection errors

**After** (Singleton Container):
```java
public abstract class PostgreSQLTestContainer {
    private static final PostgreSQLContainer<?> POSTGRES_CONTAINER;
    
    static {
        POSTGRES_CONTAINER = ...;
        POSTGRES_CONTAINER.start();  // Starts ONCE
    }
}
```
✅ Container runs throughout all tests → No errors

## Technology Stack

- Spring Boot 3.2.0
- REST-Assured 5.4.0
- Testcontainers (Singleton Pattern)
- PostgreSQL
- JUnit 5

---

**Based on Level 11** - All previous tests still pass!  
**FIXED**: Testcontainers singleton pattern for reliable test execution
