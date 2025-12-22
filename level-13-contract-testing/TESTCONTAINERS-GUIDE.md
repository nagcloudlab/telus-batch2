# 🐳 Testcontainers Quick Reference Guide

## 🚀 Quick Start

### Prerequisites
```bash
# Check Docker is running
docker --version
# Should show: Docker version 20.x.x

# Check Docker daemon
docker ps
# Should show: CONTAINER ID  IMAGE  ...
```

---

## 📦 What's Included

```
level-11-testcontainers/
├── pom.xml                          ✅ Testcontainers configured
├── docker-compose.yml               ✅ Local PostgreSQL
├── README.md                        ✅ Overview
├── TESTCONTAINERS-GUIDE.md          ✅ This file
├── src/
│   ├── main/
│   │   ├── java/                    ✅ All source code
│   │   └── resources/
│   │       ├── application.yml            (H2 - quick dev)
│   │       └── application-postgres.yml   (Local PostgreSQL)
│   └── test/
│       ├── java/
│       │   └── com/npci/transfer/
│       │       ├── config/
│       │       │   └── PostgreSQLTestContainer.java  ← Extend this!
│       │       ├── component/
│       │       │   ├── AccountComponentTest.java     ← Real PostgreSQL
│       │       │   └── TransferComponentTest.java    ← Real PostgreSQL
│       │       ├── service/                          ← Unit tests (mocked)
│       │       └── controller/                       ← Unit tests (mocked)
│       └── resources/
│           └── application-test.yml  ← Test config
```

---

## 🎯 Running Tests

### All Tests (Unit + Component)
```bash
mvn clean test

# First run: Downloads postgres:15-alpine (~80MB)
# Time: ~45 seconds
# Subsequent: ~15 seconds
```

### Only Component Tests
```bash
mvn test -Dtest="*ComponentTest"

# Runs only Testcontainers tests
# Time: ~12 seconds
```

### Only Unit Tests (Fast)
```bash
mvn test -Dtest="!*ComponentTest"

# Skips Testcontainers tests
# Time: ~5 seconds
```

---

## 🐘 Local PostgreSQL Development

### Start PostgreSQL
```bash
# Start containers
docker-compose up -d

# Verify running
docker ps
# Should show: postgres and pgadmin containers
```

### Run Application
```bash
# Run with postgres profile
mvn spring-boot:run -Dspring-boot.run.profiles=postgres

# App connects to localhost:5432
# Access: http://localhost:8080
```

### Access PgAdmin
```
URL:      http://localhost:5050
Email:    admin@npci.com
Password: admin

Add Server:
- Name: Local
- Host: postgres (container name)
- Port: 5432
- Database: transfer_db
- Username: transfer_user
- Password: transfer_pass
```

### Stop PostgreSQL
```bash
# Stop containers
docker-compose down

# Stop and remove volumes
docker-compose down -v
```

---

## 📝 Writing Component Tests

### Step 1: Extend Base Class

```java
import com.npci.transfer.config.PostgreSQLTestContainer;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class MyComponentTest extends PostgreSQLTestContainer {
    // Your tests here
}
```

**Key Annotations**:
- `@DataJpaTest`: Spring Boot slice test
- `@AutoConfigureTestDatabase(replace = NONE)`: Don't use H2, use Testcontainers!
- `@ActiveProfiles("test")`: Use application-test.yml
- `extends PostgreSQLTestContainer`: Automatic PostgreSQL container

---

### Step 2: Write Tests

```java
@Autowired
private AccountRepository accountRepository;

@Test
void shouldEnforceUniqueConstraint() {
    // Given
    Account account1 = Account.builder()
            .upiId("alice@okaxis")
            .build();
    accountRepository.save(account1);
    
    Account account2 = Account.builder()
            .upiId("alice@okaxis") // DUPLICATE!
            .build();
    
    // When/Then - PostgreSQL enforces unique constraint
    assertThrows(DataIntegrityViolationException.class, () -> {
        accountRepository.save(account2);
        accountRepository.flush(); // Force DB operation
    });
}
```

---

## 🔍 Key Differences: H2 vs PostgreSQL

### 1. Unique Constraints

**H2** (Lenient):
```java
// May allow duplicates depending on configuration
account1.setUpiId("alice@okaxis");
account2.setUpiId("alice@okaxis");
accountRepository.save(account1); // OK
accountRepository.save(account2); // Might be OK! ❌
```

**PostgreSQL** (Strict):
```java
// Always enforces unique constraint
accountRepository.save(account1); // OK
accountRepository.save(account2); // ALWAYS fails ✅
// Exception: duplicate key value violates unique constraint
```

---

### 2. SQL Dialect

**H2**:
```sql
SELECT DATEDIFF('DAY', start_date, end_date);
```

**PostgreSQL**:
```sql
SELECT date_part('day', end_date - start_date);
```

**Impact**: Queries work in H2, fail in PostgreSQL!

---

### 3. Decimal Precision

**H2**:
```java
BigDecimal amount = new BigDecimal("12345.67");
// May be stored as: 12345.669999999
```

**PostgreSQL**:
```java
BigDecimal amount = new BigDecimal("12345.67");
// Stored exactly as: 12345.67 ✅
```

---

### 4. Transaction Isolation

**H2**: Basic transaction support  
**PostgreSQL**: Full ACID with configurable isolation levels  

```java
@Test
@Transactional
void shouldRollbackOnError() {
    // Make changes
    transferService.transfer(request);
    
    // Error occurs...
    
    // PostgreSQL: Full rollback ✅
    // H2: May have partial changes ❌
}
```

---

## 🔧 Testcontainers Configuration

### Base Configuration

```java
@Testcontainers
public abstract class PostgreSQLTestContainer {
    
    @Container
    static PostgreSQLContainer<?> postgres = 
        new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("transfer_test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true); // Reuse container for speed
    
    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
```

**Why `postgres:15-alpine`?**
- Small: ~80MB (vs postgres:15 at 350MB+)
- Fast: Quick download and startup
- Production: Real PostgreSQL 15 features

**Why `static`?**
- Shared across all tests in class
- Container starts once, reused for all tests
- Much faster!

**Why `.withReuse(true)`?**
- Reuses container across test classes
- Even faster test execution
- Testcontainers manages lifecycle

---

## 🐛 Troubleshooting

### Issue 1: Docker Not Running

**Error**:
```
Could not find a valid Docker environment
```

**Solution**:
```bash
# Mac/Windows: Start Docker Desktop
# Linux:
sudo systemctl start docker

# Verify
docker ps
```

---

### Issue 2: Port Already in Use

**Error**:
```
Bind for 0.0.0.0:5432 failed: port is already allocated
```

**Solution**:
```bash
# Stop local PostgreSQL
sudo systemctl stop postgresql

# Or let Testcontainers use random port (automatic)
```

---

### Issue 3: Slow First Run

**Symptom**: Tests take 2+ minutes first time

**Cause**: Downloading postgres:15-alpine image

**Solution**:
```bash
# Pre-download image
docker pull postgres:15-alpine

# Subsequent runs: ~15 seconds ✅
```

---

### Issue 4: Tests Fail in CI

**Error**: Tests pass locally, fail in GitHub Actions

**Solution**: Ensure Docker is available
```yaml
# GitHub Actions has Docker by default
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
      - run: mvn test
    # Docker available automatically! ✅
```

---

### Issue 5: Container Not Stopping

**Symptom**: Docker containers left running

**Solution**:
```bash
# List containers
docker ps -a

# Stop specific container
docker stop <container_id>

# Stop all
docker stop $(docker ps -aq)

# Clean up
docker system prune
```

---

## 📊 Performance Tips

### 1. Use Alpine Images
```java
✅ postgres:15-alpine  // 80MB
❌ postgres:15         // 350MB+
```

### 2. Enable Container Reuse
```java
.withReuse(true)  // Faster tests
```

### 3. Shared Container per Class
```java
static PostgreSQLContainer<?> postgres  // Shared
```

### 4. Run Unit Tests First
```bash
# Quick feedback
mvn test -Dtest="!*ComponentTest"  // 5 sec

# Full confidence
mvn test  // 15 sec
```

---

## 🎯 Best Practices

### 1. When to Use Component Tests

**Use for**:
- ✅ Database constraints (unique, foreign key)
- ✅ Transaction behavior
- ✅ SQL queries
- ✅ Data persistence verification

**Don't use for**:
- ❌ Business logic (use unit tests)
- ❌ Validation rules (use unit tests)
- ❌ Calculations (use unit tests)

**Rule**: 70% unit tests, 30% component tests

---

### 2. Test Organization

```
src/test/java/
├── unit/               ← Fast, mocked
│   ├── service/
│   └── controller/
└── component/          ← Slower, real DB
    └── AccountComponentTest.java
```

---

### 3. CI/CD Strategy

```
Local Development:
- Quick runs: H2 (fast feedback)
- Full runs: Testcontainers (before push)

CI/CD Pipeline:
- Always run: Testcontainers
- Quality gate: Must pass to merge
```

---

## 📈 Metrics Comparison

### Before Testcontainers (H2 Only)

```
Tests: 80
Database: H2
Time: 5 seconds
Bugs Caught: Unit-level only
Production Parity: LOW
```

### After Testcontainers

```
Tests: 86 (80 unit + 6 component)
Database: H2 + PostgreSQL
Time: 15 seconds (worth it!)
Bugs Caught: Unit + Database level
Production Parity: HIGH ✅
```

---

## 🔗 Additional Resources

### Testcontainers
- Docs: https://www.testcontainers.org/
- Spring Boot: https://www.testcontainers.org/modules/databases/postgres/
- Examples: https://github.com/testcontainers/testcontainers-java

### PostgreSQL
- Docs: https://www.postgresql.org/docs/15/
- Data Types: https://www.postgresql.org/docs/15/datatype.html
- Transactions: https://www.postgresql.org/docs/15/tutorial-transactions.html

---

## 💡 Key Takeaways

```
✅ H2 is fast but NOT production-equivalent
✅ Testcontainers = Real PostgreSQL in tests
✅ Component tests verify database integration
✅ Extra 10 seconds = Prevents production bugs
✅ Test what you deploy!
```

---

**Happy Testing with Real Databases!** 🐘
