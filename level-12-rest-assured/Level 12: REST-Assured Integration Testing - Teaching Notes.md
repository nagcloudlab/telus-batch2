# Level 12: REST-Assured Integration Testing - Teaching Notes

## Session Overview
- Introduction to REST-Assured framework
- Integration testing vs Unit/Component testing
- HTTP API testing patterns
- Testcontainers singleton pattern (critical fix)
- Complete request/response validation
- Building production-ready test suites

---

## Part 1: Understanding Integration Testing

### What is Integration Testing?

**Definition**
- Tests the complete application flow end-to-end
- Tests HTTP request → Controller → Service → Repository → Database
- Uses real HTTP server, real database, real network calls
- Validates API contracts and integration points

**Integration vs Other Test Types**

| Aspect | Unit Test | Component Test | Integration Test |
|--------|-----------|----------------|------------------|
| **Scope** | Single class/method | Multiple layers (Service + Repository) | Complete HTTP flow |
| **Dependencies** | Mocked | Real database | Real server + database |
| **Speed** | Fastest (ms) | Medium (100-500ms) | Slower (500-2000ms) |
| **Network** | No | No | Yes (HTTP calls) |
| **Database** | No | Yes (Testcontainers) | Yes (Testcontainers) |
| **Server** | No | No | Yes (embedded Tomcat) |
| **Example** | `TransferServiceTest` | `TransferComponentTest` | `TransferApiIntegrationTest` |

**Test Pyramid Refresher**
```
        /\
       /  \  ← Integration Tests (10-20%)
      /    \
     /------\  ← Component Tests (20-30%)
    /        \
   /----------\  ← Unit Tests (50-70%)
  /            \
```

**When to Use Integration Tests**
- ✅ API contract validation
- ✅ End-to-end flow verification
- ✅ HTTP status code testing
- ✅ JSON response format validation
- ✅ Error handling across layers
- ✅ Authentication/Authorization flows
- ❌ NOT for business logic (use unit tests)
- ❌ NOT for database queries (use component tests)

---

## Part 2: Introduction to REST-Assured

### What is REST-Assured?

**Overview**
- Java library for testing REST APIs
- Provides readable DSL (Domain Specific Language)
- Industry standard (Netflix, Amazon, Spotify, banks)
- Integrates with JUnit 5, TestNG, Spring Boot
- Simplifies HTTP testing dramatically

**Maven Dependency**
```xml
<dependency>
    <groupId>io.rest-assured</groupId>
    <artifactId>rest-assured</artifactId>
    <version>5.4.0</version>
    <scope>test</scope>
</dependency>
```

### REST-Assured vs Traditional Approach

**Without REST-Assured** (Traditional)
```java
// Complex, verbose code
HttpClient client = HttpClient.newHttpClient();
HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create("http://localhost:8080/v1/transfers"))
    .header("Content-Type", "application/json")
    .POST(HttpRequest.BodyPublishers.ofString("{...}"))
    .build();
    
HttpResponse<String> response = client.send(request, 
    HttpResponse.BodyHandlers.ofString());

assertEquals(200, response.statusCode());

ObjectMapper mapper = new ObjectMapper();
JsonNode json = mapper.readTree(response.body());
assertEquals("SUCCESS", json.get("status").asText());
```

**With REST-Assured** (Clean!)
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

**Benefits**
- ✅ Readable - reads like plain English
- ✅ Less code - 5 lines vs 15+ lines
- ✅ Built-in assertions - no manual JSON parsing
- ✅ Better error messages - shows request/response on failure
- ✅ Chainable methods - fluent API style

---

## Part 3: REST-Assured Syntax Deep Dive

### The Given-When-Then Pattern

**Structure**
```java
given()        // SETUP: Configure request
    .contentType(ContentType.JSON)
    .body(requestJson)
    .header("Authorization", "Bearer token")
.when()        // ACTION: Execute HTTP call
    .post("/transfers")
.then()        // VERIFY: Assert response
    .statusCode(200)
    .body("status", equalTo("SUCCESS"));
```

**Three Phases Explained**

1. **GIVEN** (Setup Phase)
   - Set request headers
   - Set request body
   - Set query parameters
   - Configure authentication

2. **WHEN** (Action Phase)
   - Execute HTTP method (GET, POST, PUT, DELETE)
   - Specify endpoint path
   - Actually make the HTTP call

3. **THEN** (Verification Phase)
   - Assert status code
   - Assert response body
   - Assert response headers
   - Extract values for further testing

### Common REST-Assured Methods

**Request Configuration**
```java
given()
    .contentType(ContentType.JSON)           // Set Content-Type header
    .accept(ContentType.JSON)                // Set Accept header
    .header("Custom-Header", "value")        // Set custom header
    .body(requestObject)                     // Set request body (auto-converts to JSON)
    .queryParam("page", 1)                   // Add query parameter (?page=1)
    .pathParam("id", "123")                  // Add path parameter (/users/{id})
```

**HTTP Methods**
```java
.when()
    .get("/accounts")                        // GET request
    .post("/transfers")                      // POST request
    .put("/accounts/{id}")                   // PUT request
    .delete("/accounts/{id}")                // DELETE request
    .patch("/accounts/{id}")                 // PATCH request
```

**Response Assertions**
```java
.then()
    .statusCode(200)                         // Assert status code
    .statusCode(isOneOf(200, 201))          // Status is 200 OR 201
    .contentType(ContentType.JSON)           // Assert content type
    .header("X-Custom", "value")             // Assert header value
    .body("status", equalTo("SUCCESS"))      // Assert JSON field
    .body("amount", equalTo(500.0f))         // Assert number field
    .body("transactionId", notNullValue())   // Assert not null
    .body("errors", hasItem("Invalid UPI")) // Assert list contains item
    .time(lessThan(2000L))                   // Assert response time < 2s
```

### JSON Path Assertions

**Simple Fields**
```json
{
  "status": "SUCCESS",
  "amount": 500.00
}
```
```java
.body("status", equalTo("SUCCESS"))
.body("amount", equalTo(500.0f))
```

**Nested Fields**
```json
{
  "transaction": {
    "id": "TXN123",
    "status": "SUCCESS"
  }
}
```
```java
.body("transaction.id", equalTo("TXN123"))
.body("transaction.status", equalTo("SUCCESS"))
```

**Lists/Arrays**
```json
{
  "errors": ["Invalid UPI", "Amount too high"]
}
```
```java
.body("errors", hasSize(2))
.body("errors", hasItem("Invalid UPI"))
.body("errors[0]", equalTo("Invalid UPI"))
```

**Hamcrest Matchers**
```java
// Common matchers
equalTo("value")                  // Exact match
notNullValue()                    // Not null
nullValue()                       // Is null
hasItem("value")                  // List contains item
hasItems("val1", "val2")         // List contains multiple items
hasSize(3)                        // Collection size is 3
containsString("substring")       // String contains
startsWith("prefix")              // String starts with
greaterThan(100)                  // Number > 100
lessThan(100)                     // Number < 100
isOneOf("A", "B")                 // Value is one of
```

---

## Part 4: Integration Test Setup in Spring Boot

### BaseIntegrationTest Class

**Purpose**
- Provides common configuration for all integration tests
- Configures REST-Assured with server port
- Extends PostgreSQLTestContainer for database
- Sets up Spring Boot test environment

**Code Breakdown**
```java
@SpringBootTest(webEnvironment = RANDOM_PORT)  // Start real server on random port
@ActiveProfiles("test")                        // Use test configuration
public abstract class BaseIntegrationTest extends PostgreSQLTestContainer {
    
    @LocalServerPort                           // Inject actual port number
    private int port;
    
    @BeforeEach
    void setUpRestAssured() {
        RestAssured.port = port;               // Configure REST-Assured port
        RestAssured.basePath = "/v1";          // Set base path for all requests
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
}
```

**Annotations Explained**

1. **@SpringBootTest(webEnvironment = RANDOM_PORT)**
   - Starts entire Spring application context
   - Launches embedded Tomcat server
   - Uses random available port (avoids conflicts)
   - Alternative: `DEFINED_PORT` (uses application.yml port)

2. **@ActiveProfiles("test")**
   - Activates `application-test.yml` configuration
   - Allows test-specific settings
   - Different from production configuration

3. **@LocalServerPort**
   - Injects actual port number chosen by Spring
   - Required because we use RANDOM_PORT
   - Allows REST-Assured to connect to correct port

**Why Random Port?**
- ✅ Prevents port conflicts when running multiple test suites
- ✅ Allows parallel test execution
- ✅ Works on CI/CD servers with limited ports
- ✅ More reliable in team environments

**REST-Assured Configuration**
```java
RestAssured.port = port;           // "Connect to port 54321" (example)
RestAssured.basePath = "/v1";      // "All URLs start with /v1"
                                   // So .post("/transfers") becomes http://localhost:54321/v1/transfers

RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
// If test fails, automatically print request and response for debugging
```

---

## Part 5: Writing Integration Tests

### Test Structure Pattern

**Standard Integration Test**
```java
@DisplayName("Transfer API Integration Tests")
class TransferApiIntegrationTest extends BaseIntegrationTest {
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @BeforeEach
    void setUp() {
        // Clean database
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        
        // Create test data
        accountRepository.save(sourceAccount);
        accountRepository.save(destinationAccount);
    }
    
    @Test
    void shouldSuccessfullyInitiateTransfer() {
        // Test implementation
    }
}
```

**Key Points**
- Each test starts with clean database
- Test data created in @BeforeEach
- Spring Data JPA's `deleteAll()` is automatically transactional
- No need for manual transaction management

### Example Test Cases

**1. Success Scenario (200)**
```java
@Test
@DisplayName("Should successfully initiate transfer")
void shouldSuccessfullyInitiateTransfer() {
    given()
        .contentType(ContentType.JSON)
        .body("""
            {
                "sourceUPI": "alice@okaxis",
                "destinationUPI": "bob@paytm",
                "amount": 500.00,
                "remarks": "Test transfer"
            }
            """)
    .when()
        .post("/transfers")
    .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("transactionId", notNullValue())
        .body("status", equalTo("SUCCESS"))
        .body("amount", equalTo(500.0f))
        .body("sourceUPI", equalTo("alice@okaxis"))
        .body("destinationUPI", equalTo("bob@paytm"));
}
```

**Teaching Points**
- Uses text blocks (`"""..."""`) for readable JSON
- Validates status code first (most important)
- Validates content type (JSON expected)
- Validates all important response fields
- Uses Hamcrest matchers for assertions

**2. Business Error Scenario (400)**
```java
@Test
@DisplayName("Should return 400 for insufficient balance")
void shouldReturnBadRequestForInsufficientBalance() {
    given()
        .contentType(ContentType.JSON)
        .body("""
            {
                "sourceUPI": "alice@okaxis",
                "destinationUPI": "bob@paytm",
                "amount": 15000.00
            }
            """)
    .when()
        .post("/transfers")
    .then()
        .statusCode(400)
        .body("error", containsStringIgnoringCase("insufficient"));
}
```

**Teaching Points**
- Tests business rule validation
- Amount (15000) exceeds balance (10000)
- Validates 400 status code for bad request
- Uses `containsStringIgnoringCase` for flexible assertion
- Error message may vary, so check for keyword

**3. Not Found Scenario (404)**
```java
@Test
@DisplayName("Should return 404 for non-existent source account")
void shouldReturnNotFoundForNonExistentSource() {
    given()
        .contentType(ContentType.JSON)
        .body("""
            {
                "sourceUPI": "nonexistent@fake",
                "destinationUPI": "bob@paytm",
                "amount": 100.00
            }
            """)
    .when()
        .post("/transfers")
    .then()
        .statusCode(404)
        .body("error", containsStringIgnoringCase("not found"));
}
```

**Teaching Points**
- Tests error handling for missing resources
- 404 is standard HTTP code for "Not Found"
- GlobalExceptionHandler catches AccountNotFoundException
- Returns appropriate error response

**4. Validation Error Scenario (400)**
```java
@Test
@DisplayName("Should return 400 for zero amount")
void shouldReturnBadRequestForZeroAmount() {
    given()
        .contentType(ContentType.JSON)
        .body("""
            {
                "sourceUPI": "alice@okaxis",
                "destinationUPI": "bob@paytm",
                "amount": 0.00
            }
            """)
    .when()
        .post("/transfers")
    .then()
        .statusCode(400)
        .body("errors", hasItem(containsStringIgnoringCase("amount")));
}
```

**Teaching Points**
- Tests @Valid annotation validation
- TransferRequest has @DecimalMin("0.01") on amount
- Spring Boot validation triggers MethodArgumentNotValidException
- GlobalExceptionHandler populates `errors` list
- Response structure: `{"errors": ["Amount must be greater than 0"]}`

**5. Multiple Sequential Operations**
```java
@Test
@DisplayName("Should process multiple sequential transfers")
void shouldProcessMultipleTransfers() {
    // First transfer
    given()
        .contentType(ContentType.JSON)
        .body("""
            {
                "sourceUPI": "alice@okaxis",
                "destinationUPI": "bob@paytm",
                "amount": 100.00
            }
            """)
    .when()
        .post("/transfers")
    .then()
        .statusCode(200)
        .body("status", equalTo("SUCCESS"));
    
    // Second transfer
    given()
        .contentType(ContentType.JSON)
        .body("""
            {
                "sourceUPI": "alice@okaxis",
                "destinationUPI": "bob@paytm",
                "amount": 200.00
            }
            """)
    .when()
        .post("/transfers")
    .then()
        .statusCode(200)
        .body("status", equalTo("SUCCESS"));
}
```

**Teaching Points**
- Tests stateful behavior
- First transfer deducts 100 from balance
- Second transfer should still work (balance remaining)
- Validates system handles sequential operations
- Each request is independent HTTP call

---

## Part 6: CRITICAL - Testcontainers Singleton Pattern

### The Problem - What Happened in Your Code

**Original Code (Broken)**
```java
@Testcontainers  // This was the problem!
public abstract class PostgreSQLTestContainer {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(...)
        .withDatabaseName("transfer_test_db");
}
```

**What Happened**
1. `AccountComponentTest` runs
   - @Testcontainers creates NEW container
   - Container starts on port 55492
   - Tests run successfully ✅
   - @Testcontainers **STOPS container** (cleanup)

2. `TransferComponentTest` runs
   - @Testcontainers creates NEW container
   - Container starts on port 55493
   - Tests run successfully ✅
   - @Testcontainers **STOPS container** (cleanup)

3. `TransferApiIntegrationTest` runs
   - @Testcontainers creates NEW container
   - Tries to start container...
   - HikariCP tries to connect to old port 55492
   - **ERROR: Connection refused!** ❌

**Why It Failed**
- Each test class got its own container
- Container lifecycle tied to test class
- Port numbers changed between runs
- Spring Boot cached old JDBC URL
- Connection pool held references to dead container

**Error Messages**
```
Connection to localhost:55492 refused
HikariPool-2 - Connection is not available
Tests run: 101, Failures: 0, Errors: 5
```

### The Solution - Singleton Pattern

**Fixed Code**
```java
public abstract class PostgreSQLTestContainer {  // No @Testcontainers!
    
    // SINGLETON: ONE container for ALL tests
    private static final PostgreSQLContainer<?> POSTGRES_CONTAINER;
    
    static {
        POSTGRES_CONTAINER = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:15-alpine")
        )
        .withDatabaseName("transfer_test_db")
        .withUsername("test_user")
        .withPassword("test_password")
        .withReuse(true);  // Reuse container across test runs
        
        POSTGRES_CONTAINER.start();  // Start ONCE
    }
    
    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);
    }
}
```

**How It Works**
1. JVM loads `PostgreSQLTestContainer` class
2. Static initializer block runs ONCE
3. Container starts ONCE on port (e.g., 55492)
4. All test classes extend this base class
5. All tests use SAME container
6. Container stops ONLY when JVM exits

**Execution Flow**
```
JVM Starts
  ↓
Static block runs → Container starts on port 55492
  ↓
AccountComponentTest (6 tests) → Uses port 55492 ✅
  ↓
AccountRepositoryTest (10 tests) → Uses port 55492 ✅
  ↓
TransferComponentTest (5 tests) → Uses port 55492 ✅
  ↓
TransferApiIntegrationTest (10 tests) → Uses port 55492 ✅
  ↓
All other tests (70 tests) → Mock-based, no container needed
  ↓
Tests complete → Container stops
  ↓
JVM exits
```

**Benefits**
- ✅ Container starts **once** (5 seconds)
- ✅ All tests use **same container**
- ✅ No port conflicts
- ✅ 30% faster test execution
- ✅ Reliable connections
- ✅ Industry standard pattern

**Performance Comparison**

| Pattern | Container Startups | Test Time |
|---------|-------------------|-----------|
| Per-Class (@Testcontainers) | 4 times (20s) | ~50s |
| Singleton (static final) | 1 time (5s) | ~35s |
| **Savings** | **-15s** | **-30%** |

### @DynamicPropertySource Explained

**Purpose**
- Dynamically sets Spring properties at runtime
- Gets actual port/URL from running container
- Injects into Spring's environment

**How It Works**
```java
@DynamicPropertySource
static void setProperties(DynamicPropertyRegistry registry) {
    // At runtime, get actual JDBC URL from container
    String jdbcUrl = POSTGRES_CONTAINER.getJdbcUrl();
    // Example: "jdbc:postgresql://localhost:55492/transfer_test_db"
    
    // Override application-test.yml properties
    registry.add("spring.datasource.url", () -> jdbcUrl);
    registry.add("spring.datasource.username", () -> "test_user");
    registry.add("spring.datasource.password", () -> "test_password");
}
```

**Before vs After**

*application-test.yml*:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/testdb  # Wrong! Container uses random port
```

*@DynamicPropertySource* (Runtime):
```java
// Overrides yml with actual container URL
url: jdbc:postgresql://localhost:55492/transfer_test_db  ✅
```

**Why This Matters**
- Container port is random (Testcontainers behavior)
- Can't hardcode port in application-test.yml
- Must get port at runtime from container
- @DynamicPropertySource solves this perfectly

---

## Part 7: Error Response Handling

### ErrorResponse Evolution

**Level 11 (Old)**
```java
@Data
@Builder
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private Map<String, String> validationErrors;  // Only map
}
```

**Level 12 (New)**
```java
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)  // Don't include null fields in JSON
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private List<String> errors;              // NEW! For simple list
    private Map<String, String> validationErrors;  // Keep for field-specific errors
}
```

**Why Both `errors` and `validationErrors`?**

1. **`errors` (List<String>)** - Simple error messages
   ```json
   {
     "status": 400,
     "errors": ["Invalid UPI format", "Amount exceeds limit"]
   }
   ```
   - ✅ Easy to assert with REST-Assured: `.body("errors", hasItem("..."))`
   - ✅ Client-friendly: simple array of messages
   - ✅ Good for general errors

2. **`validationErrors` (Map<String, String>)** - Field-specific errors
   ```json
   {
     "status": 400,
     "validationErrors": {
       "sourceUPI": "Invalid UPI format",
       "amount": "Must be greater than 0"
     }
   }
   ```
   - ✅ Maps field name to error message
   - ✅ Useful for form validation
   - ✅ Client can highlight specific fields

**GlobalExceptionHandler Update**
```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ErrorResponse> handleValidationErrors(
        MethodArgumentNotValidException ex) {
    
    Map<String, String> errors = new HashMap<>();
    List<String> errorsList = new ArrayList<>();
    
    // Populate BOTH structures
    ex.getBindingResult().getFieldErrors().forEach(error -> {
        errors.put(error.getField(), error.getDefaultMessage());
        errorsList.add(error.getDefaultMessage());
    });
    
    ErrorResponse error = ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.BAD_REQUEST.value())
        .error("Validation Error")
        .message("Invalid request parameters")
        .errors(errorsList)              // Populate list
        .validationErrors(errors)        // Populate map
        .build();
    
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
}
```

**Teaching Points**
- Both structures populated from same source
- `errors` list for simple assertions
- `validationErrors` map for detailed client needs
- @JsonInclude prevents null fields in response
- Maintains backward compatibility

---

## Part 8: Complete Test Suite Overview

### Test Hierarchy

```
101 Total Tests
├── Unit Tests (75 tests) - No external dependencies
│   ├── TransferServiceTest (15 tests)
│   │   └── Mocks AccountRepository, TransactionRepository
│   ├── FeeCalculatorTest (36 tests)
│   │   └── Pure logic, no dependencies
│   └── TransferControllerTest (19 tests)
│       └── Mocks TransferService, uses MockMvc
│
├── Repository Tests (10 tests) - @DataJpaTest
│   └── AccountRepositoryTest (10 tests)
│       └── Uses Testcontainers PostgreSQL
│
├── Component Tests (6 tests) - @SpringBootTest + @DataJpaTest
│   └── TransferComponentTest (6 tests)
│       └── Uses Testcontainers PostgreSQL
│
└── Integration Tests (10 tests) - @SpringBootTest + REST-Assured
    └── TransferApiIntegrationTest (10 tests)
        └── Uses Testcontainers PostgreSQL + embedded Tomcat
```

### Test Categories Explained

**1. Unit Tests (75)**
- Fastest execution (<100ms total)
- No external dependencies
- Mock all dependencies
- Test business logic in isolation
- Examples:
  - Fee calculation formulas
  - Transfer validation rules
  - Service orchestration logic

**2. Repository Tests (10)**
- Medium speed (1-2 seconds)
- Real PostgreSQL via Testcontainers
- No HTTP, no controllers
- Test database operations
- Examples:
  - CRUD operations
  - Custom query methods
  - Unique constraint validation

**3. Component Tests (6)**
- Medium-slow speed (2-5 seconds)
- Real database + real service layer
- No HTTP layer
- Test multi-layer interactions
- Examples:
  - Service → Repository → Database flow
  - Transaction rollback behavior
  - Data persistence verification

**4. Integration Tests (10)**
- Slowest (5-10 seconds)
- Complete HTTP → DB flow
- Real server on random port
- Test API contracts
- Examples:
  - HTTP status codes
  - JSON response formats
  - Error handling across all layers
  - Multiple sequential API calls

---

## Part 9: Running and Debugging Tests

### Running Tests

**Run All Tests**
```bash
mvn clean test
```

**Run Specific Test Class**
```bash
mvn test -Dtest=TransferApiIntegrationTest
```

**Run Single Test Method**
```bash
mvn test -Dtest=TransferApiIntegrationTest#shouldSuccessfullyInitiateTransfer
```

**Skip Tests**
```bash
mvn clean install -DskipTests
```

### Reading Test Output

**Success Output**
```
[INFO] Running com.npci.transfer.integration.TransferApiIntegrationTest
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
```

**Failure Output**
```
[ERROR] shouldSuccessfullyInitiateTransfer  Time elapsed: 1.234 s  <<< FAILURE!
java.lang.AssertionError: 
Expected status code <200> but was <400>.
```

**REST-Assured Detailed Output** (when test fails)
```
Request method:   POST
Request URI:      http://localhost:54321/v1/transfers
Request params:   <none>
Headers:          Content-Type=application/json
Body:
{
  "sourceUPI": "alice@okaxis",
  "destinationUPI": "bob@paytm",
  "amount": 500.00
}

HTTP/1.1 400 Bad Request
Content-Type: application/json

{
  "status": 400,
  "errors": ["Amount must be greater than 0"]
}
```

### Common Issues and Solutions

**Issue 1: Connection Refused**
```
Connection to localhost:55492 refused
```
**Solution**: Use singleton Testcontainers pattern (already fixed in Level 12)

**Issue 2: Port Already in Use**
```
Address already in use: bind
```
**Solution**: 
- Use `RANDOM_PORT` in @SpringBootTest
- Kill process using port: `lsof -i :8080` then `kill -9 <PID>`

**Issue 3: Tests Pass Individually, Fail Together**
```
mvn test -Dtest=TestA  ✅
mvn test -Dtest=TestB  ✅
mvn test              ❌
```
**Solution**: 
- Shared state between tests
- Missing @BeforeEach cleanup
- Use singleton Testcontainers

**Issue 4: Slow Test Execution**
```
Tests run: 101, Time: 2 minutes
```
**Solution**:
- Use singleton Testcontainers (30% faster)
- Minimize test data creation
- Use @DirtiesContext sparingly
- Parallel test execution (advanced)

---

## Part 10: Best Practices

### Integration Testing Best Practices

**DO ✅**
- Test API contracts (status codes, response format)
- Test error handling across all layers
- Use realistic test data
- Clean database before each test
- Use meaningful test names
- Assert on important fields only
- Group related tests in same class
- Use @DisplayName for clarity

**DON'T ❌**
- Test business logic (use unit tests)
- Test every possible combination (use unit tests)
- Share state between tests
- Hardcode URLs or ports
- Assert on timestamps (they vary)
- Over-assert (brittle tests)
- Ignore test failures
- Skip cleanup in @BeforeEach

### REST-Assured Best Practices

**DO ✅**
```java
// Clear, readable structure
given()
    .contentType(ContentType.JSON)
    .body(request)
.when()
    .post("/transfers")
.then()
    .statusCode(200)
    .body("status", equalTo("SUCCESS"));
```

**DON'T ❌**
```java
// Unclear, messy structure
RestAssured.given().contentType("application/json").body(request)
.when().post("/transfers").then().statusCode(200).body("status", equalTo("SUCCESS"));
```

**Extract Common Setup**
```java
// Good
private RequestSpecification baseRequest() {
    return given()
        .contentType(ContentType.JSON)
        .header("X-API-Version", "1.0");
}

@Test
void test() {
    baseRequest()
        .body(request)
    .when()
        .post("/transfers")
    .then()
        .statusCode(200);
}
```

### Testcontainers Best Practices

**DO ✅ Singleton Pattern**
```java
public abstract class PostgreSQLTestContainer {
    private static final PostgreSQLContainer<?> POSTGRES_CONTAINER;
    
    static {
        POSTGRES_CONTAINER = new PostgreSQLContainer<>(...);
        POSTGRES_CONTAINER.start();
    }
}
```

**DON'T ❌ Per-Class Pattern**
```java
@Testcontainers
public abstract class PostgreSQLTestContainer {
    @Container
    static PostgreSQLContainer<?> postgres = ...;  // New container per class!
}
```

**Use @DynamicPropertySource**
```java
@DynamicPropertySource
static void setProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
}
```

---

## Part 11: Real-World Application

### Industry Usage

**Companies Using REST-Assured**
- Netflix (API testing)
- Amazon (microservices testing)
- Spotify (integration testing)
- Major banks (payment API testing)
- E-commerce platforms (checkout flow testing)

**Why REST-Assured is Popular**
- Readable tests (non-technical stakeholders can understand)
- Fast feedback on API changes
- Prevents breaking changes in production
- Documents API behavior through tests
- Integrates with CI/CD pipelines

### Banking/Payment Testing Scenarios

**Scenario 1: UPI Transfer Flow**
```java
// Test complete UPI transfer
given()
    .contentType(ContentType.JSON)
    .body(transferRequest)
.when()
    .post("/v1/transfers")
.then()
    .statusCode(200)
    .body("status", equalTo("SUCCESS"))
    .body("transactionId", matchesPattern("TXN-\\d{8}-\\d{4}"))
    .body("fee", greaterThan(0.0f));
```

**Scenario 2: Insufficient Balance**
```java
// Test overdraft protection
given()
    .body(largeAmountRequest)
.when()
    .post("/v1/transfers")
.then()
    .statusCode(400)
    .body("error", containsString("Insufficient balance"));
```

**Scenario 3: Daily Limit Validation**
```java
// Test regulatory compliance
given()
    .body(exceedsLimitRequest)
.when()
    .post("/v1/transfers")
.then()
    .statusCode(400)
    .body("errors", hasItem(containsString("daily limit")));
```

### Production Deployment Checklist

Before deploying API to production:

- ✅ All integration tests pass
- ✅ All unit tests pass
- ✅ All component tests pass
- ✅ Error scenarios tested
- ✅ Performance tests run (response time < 2s)
- ✅ Security tests pass
- ✅ Load tests pass
- ✅ API documentation updated
- ✅ Monitoring/alerts configured
- ✅ Rollback plan prepared

---

## Part 12: Comparison with Previous Levels

### Level 10: Mutation Testing
- **Focus**: Code quality, test effectiveness
- **Tool**: PIT mutation testing
- **Output**: Mutation score, killed/survived mutants
- **Purpose**: Ensure tests actually validate logic

### Level 11: Testcontainers
- **Focus**: Database integration testing
- **Tool**: Testcontainers PostgreSQL
- **Scope**: Repository + Component tests
- **Purpose**: Test with real database

### Level 12: REST-Assured
- **Focus**: API integration testing
- **Tool**: REST-Assured + Testcontainers
- **Scope**: Complete HTTP flow
- **Purpose**: Test API contracts end-to-end

### Progressive Testing Strategy

```
Level 10: Mutation Testing
    ↓ Validated test quality
Level 11: Testcontainers
    ↓ Added database integration
Level 12: REST-Assured
    ↓ Added HTTP API testing
Production Deployment
```

---

## Part 13: Key Takeaways

### Technical Concepts Mastered

1. **Integration Testing**
   - Complete end-to-end testing
   - HTTP → Controller → Service → Repository → Database
   - Real server + real database

2. **REST-Assured Framework**
   - Given-When-Then pattern
   - Readable DSL for API testing
   - JSON path assertions
   - Hamcrest matchers

3. **Testcontainers Singleton Pattern**
   - ONE container for ALL tests
   - Prevents lifecycle issues
   - Industry standard approach
   - 30% faster execution

4. **Spring Boot Test Configuration**
   - @SpringBootTest with RANDOM_PORT
   - @DynamicPropertySource
   - @LocalServerPort
   - @ActiveProfiles

5. **Error Response Handling**
   - List-based errors for simple assertions
   - Map-based validationErrors for field-specific errors
   - GlobalExceptionHandler integration
   - @JsonInclude for clean responses

### Common Pitfalls to Avoid

1. ❌ **Per-Class Testcontainers** → Use singleton pattern
2. ❌ **Hardcoded ports** → Use RANDOM_PORT
3. ❌ **No database cleanup** → Add @BeforeEach cleanup
4. ❌ **Over-asserting** → Assert important fields only
5. ❌ **Testing business logic in integration tests** → Use unit tests
6. ❌ **Ignoring test failures** → Fix immediately
7. ❌ **No @DisplayName** → Add descriptive names
8. ❌ **Shared state between tests** → Each test independent

### Production-Ready Checklist

- ✅ 101 tests passing
- ✅ Unit tests (fast feedback)
- ✅ Component tests (database integration)
- ✅ Integration tests (API contracts)
- ✅ Mutation testing (test quality)
- ✅ Testcontainers (real database)
- ✅ REST-Assured (readable API tests)
- ✅ Error handling (comprehensive)
- ✅ Singleton pattern (performance)
- ✅ Clean architecture (maintainable)

---

## Summary

**Level 12 Achievements**
- ✅ Learned REST-Assured framework
- ✅ Implemented 10 integration tests
- ✅ Fixed Testcontainers singleton pattern
- ✅ Enhanced error response structure
- ✅ Tested complete HTTP → DB flow
- ✅ Industry-standard test patterns
- ✅ Production-ready test suite

**Next Steps**
- Practice writing more integration tests
- Experiment with different HTTP methods (PUT, DELETE)
- Add authentication/authorization tests
- Implement contract testing (Pact, Spring Cloud Contract)
- Performance testing with Gatling/JMeter
- Explore parallel test execution

**Real-World Impact**
- Prevents production bugs
- Documents API behavior
- Enables confident refactoring
- Supports CI/CD pipelines
- Reduces manual testing
- Improves code quality

---

## Questions for Students

1. What's the difference between component tests and integration tests?
2. Why do we use singleton Testcontainers pattern?
3. What are the three parts of REST-Assured syntax?
4. When should you use `errors` list vs `validationErrors` map?
5. Why use RANDOM_PORT instead of DEFINED_PORT?
6. How does @DynamicPropertySource work?
7. What happens if you don't clean the database in @BeforeEach?
8. Why is the test pyramid important?
9. How do you debug a failing REST-Assured test?
10. What makes a good integration test?

---

## Hands-On Exercises

**Exercise 1: Add New Integration Test**
- Test invalid UPI format (both source and destination)
- Assert specific error message
- Verify 400 status code

**Exercise 2: Test Multiple Transfers**
- Transfer money three times sequentially
- Verify balance decreases correctly
- Check all transactions are recorded

**Exercise 3: Add GET Endpoint Test**
- Add GET /v1/transfers/{transactionId} endpoint
- Write integration test
- Verify 404 when transaction not found

**Exercise 4: Test Concurrent Transfers**
- Simulate two simultaneous transfers from same account
- Verify one succeeds, one fails (insufficient balance)
- Check database consistency

**Exercise 5: Performance Testing**
- Add `.time(lessThan(2000L))` assertion
- Ensure API responds within 2 seconds
- Identify slow queries

---

**End of Level 12 Teaching Notes**