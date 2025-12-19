# Level 4: Quick Start Guide

## 🚀 Get Bad Code Running in 5 Minutes

This guide helps you quickly run the "bad code" version to see the problems firsthand.

---

## Prerequisites

```bash
# Check installations
java --version   # Java 17+
mvn --version    # Maven 3.8+
curl --version   # For testing APIs
```

---

## Step 1: Build the Project (1 minute)

```bash
# Navigate to project directory
cd level-04-project-setup-bad-code

# Build the project
mvn clean install -DskipTests

# Expected output:
# [INFO] BUILD SUCCESS
```

---

## Step 2: Run the Application (1 minute)

```bash
# Start the application
mvn spring-boot:run

# Wait for:
# "Started TransferServiceApplication in X seconds"
```

Application will be available at: http://localhost:8080

---

## Step 3: Initialize Test Data (30 seconds)

```bash
# Create test accounts (alice and bob)
curl -X POST http://localhost:8080/v1/init-data

# Expected response:
# "Data initialized: alice@okaxis and bob@paytm"
```

---

## Step 4: Test the Bad Code (2 minutes)

### Test 1: Health Check

```bash
curl http://localhost:8080/v1/health

# Expected:
# {"status":"UP","timestamp":"2024-12-20T..."}
```

### Test 2: Successful Transfer

```bash
curl -X POST http://localhost:8080/v1/transfers \
  -H "Content-Type: application/json" \
  -d '{
    "sourceUPI": "alice@okaxis",
    "destinationUPI": "bob@paytm",
    "amount": 500,
    "remarks": "Test payment"
  }'

# Expected:
# {
#   "transactionId": "TXN-20241220-XXXXXX",
#   "status": "SUCCESS",
#   "sourceUPI": "alice@okaxis",
#   "destinationUPI": "bob@paytm",
#   "amount": 500.0,
#   "fee": 0.0,
#   "totalDebited": 500.0,
#   "timestamp": "2024-12-20T...",
#   "remarks": "Test payment"
# }
```

### Test 3: Get Transaction Status

```bash
# Replace XXXXXX with actual transaction ID from previous response
curl http://localhost:8080/v1/transactions/TXN-20241220-XXXXXX

# Expected:
# {transaction details}
```

### Test 4: Insufficient Balance (Error Case)

```bash
# Transfer more than alice has
curl -X POST http://localhost:8080/v1/transfers \
  -H "Content-Type: application/json" \
  -d '{
    "sourceUPI": "alice@okaxis",
    "destinationUPI": "bob@paytm",
    "amount": 50000,
    "remarks": "Too much"
  }'

# Expected:
# {"status":"FAILED","message":"Insufficient balance"}
```

### Test 5: Invalid UPI (Will Crash!)

```bash
# This will crash the application - demonstrating bad error handling
curl -X POST http://localhost:8080/v1/transfers \
  -H "Content-Type: application/json" \
  -d '{
    "sourceUPI": "nonexistent@okaxis",
    "destinationUPI": "bob@paytm",
    "amount": 100,
    "remarks": "This will crash"
  }'

# Expected:
# HTTP 500 Internal Server Error
# IndexOutOfBoundsException in logs!
```

**💥 Application crashed! This demonstrates the bad error handling.**

Restart with: `mvn spring-boot:run`

---

## Step 5: Explore H2 Console (Optional)

```bash
# Open browser to:
http://localhost:8080/h2-console

# Connection details:
JDBC URL: jdbc:h2:mem:transferdb
Username: sa
Password: (leave empty)

# Click "Connect"
```

### Query Examples:

```sql
-- View all accounts
SELECT * FROM accounts;

-- View all transactions
SELECT * FROM transactions;

-- Check alice's balance
SELECT upi_id, balance FROM accounts WHERE upi_id = 'alice@okaxis';
```

---

## Common Issues & Solutions

### Issue: Port 8080 already in use

```bash
# Find process using port 8080
lsof -i :8080

# Kill the process
kill -9 <PID>

# Or change port in application.yml
server:
  port: 8081
```

### Issue: Maven build fails

```bash
# Clean and rebuild
mvn clean install -DskipTests -U

# If still fails, check Java version
java --version  # Must be 17+
```

### Issue: Application won't start

```bash
# Check logs in terminal
# Common issues:
# 1. Port already in use
# 2. Java version mismatch
# 3. Missing dependencies

# Try clean build
mvn clean install -DskipTests
mvn spring-boot:run
```

---

## Demonstrating Bad Code Problems

### Problem 1: No Error Handling

```bash
# Transfer with invalid UPI
curl -X POST http://localhost:8080/v1/transfers \
  -H "Content-Type: application/json" \
  -d '{
    "sourceUPI": "invalid@okaxis",
    "destinationUPI": "bob@paytm",
    "amount": 100
  }'

# Result: Application crashes with IndexOutOfBoundsException
```

### Problem 2: No Validation

```bash
# Transfer negative amount
curl -X POST http://localhost:8080/v1/transfers \
  -H "Content-Type: application/json" \
  -d '{
    "sourceUPI": "alice@okaxis",
    "destinationUPI": "bob@paytm",
    "amount": -500,
    "remarks": "Negative amount"
  }'

# Result: Processes successfully! (Should be rejected)
```

### Problem 3: No Authentication

```bash
# Anyone can transfer from any account - no authentication!
curl -X POST http://localhost:8080/v1/transfers \
  -H "Content-Type: application/json" \
  -d '{
    "sourceUPI": "alice@okaxis",
    "destinationUPI": "bob@paytm",
    "amount": 100
  }'

# No authentication required - major security flaw!
```

### Problem 4: No Rate Limiting

```bash
# Spam the API - no rate limiting
for i in {1..100}; do
  curl -X POST http://localhost:8080/v1/transfers \
    -H "Content-Type: application/json" \
    -d '{
      "sourceUPI": "alice@okaxis",
      "destinationUPI": "bob@paytm",
      "amount": 1
    }' &
done

# All requests succeed - DoS vulnerability!
```

---

## Understanding the Bad Code

### Open TransferController.java

```bash
# View the terrible code
cat src/main/java/com/npci/transfer/TransferController.java
```

**Count the problems**:
- ❌ 127 lines in one method
- ❌ Direct database access in controller
- ❌ No validation
- ❌ No error handling
- ❌ Magic numbers (1000, 5.0)
- ❌ Poor variable names (src, dst, amt)
- ❌ No tests

---

## Running Code Quality Analysis

### Option 1: Manual Review

```bash
# Count lines in transfer method
# Look for magic numbers
# Identify code smells
```

### Option 2: SonarQube (if available)

```bash
# Run SonarQube scanner
mvn sonar:sonar \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=<your-token>

# View results at http://localhost:9000
```

**Expected Issues**: 48+ code smells, 3 bugs, 5 vulnerabilities

---

## Exercise for Participants

### Exercise 1: Find All Problems (15 minutes)

Review TransferController.java and list all bad practices:

**Tasks**:
1. Count magic numbers
2. Identify long methods
3. Find missing validation
4. List poor variable names
5. Note missing error handling

### Exercise 2: Try to Test It (10 minutes)

Try writing a unit test for the transfer method:

```java
@Test
void shouldTransferMoney() {
    TransferController controller = new TransferController();
    // How do you inject EntityManager?
    // How do you mock the database?
    // This is impossible!
}
```

**Realization**: Bad code is untestable!

### Exercise 3: Break It (5 minutes)

Find ways to crash the application:

**Ideas**:
- Invalid UPI IDs
- Null values
- Negative amounts
- Missing fields
- Concurrent transfers

---

## Test Scenarios (From Level 1)

| Scenario | Works? | Notes |
|----------|--------|-------|
| TS-1: Successful Transfer | ✅ Yes | Basic case works |
| TS-2: Insufficient Balance | ⚠️ Partial | Returns error but inconsistent |
| TS-3: Invalid Source UPI | ❌ Crash | IndexOutOfBoundsException |
| TS-4: Invalid Dest UPI | ❌ Crash | IndexOutOfBoundsException |
| TS-5: Invalid Format | ✅ Yes | Works (no validation!) |
| TS-6: Below Minimum | ✅ Yes | Works (no validation!) |
| TS-7: Exceeds Limit | ✅ Yes | Works (no validation!) |
| TS-8: Daily Limit | ❌ No | Not implemented |
| TS-9: Idempotency | ❌ No | Not implemented |
| TS-11: Concurrency | ❌ No | Race conditions |

**Coverage**: 2/20 scenarios (10%)

---

## What You Should Learn

By running this bad code, participants will:

1. ✅ **See It Work**: Bad code can function
2. ✅ **See It Fail**: But fails in unexpected ways
3. ✅ **See It's Untestable**: Cannot write tests
4. ✅ **See The Metrics**: 48+ code smells
5. ✅ **Feel The Pain**: Debugging is hard
6. ✅ **Want To Fix It**: Motivated for Level 6

---

## Key Takeaways

💡 **Bad code works** - until it doesn't  
💡 **No tests = no confidence** - how do you know it works?  
💡 **Crashes are expensive** - better to prevent  
💡 **Tech debt compounds** - small issues become big  
💡 **Refactoring is essential** - not optional  

---

## Cleanup

```bash
# Stop the application
# Press Ctrl+C in terminal

# Clean build artifacts
mvn clean

# Remove H2 database files
rm -rf *.db
```

---

## What's Next?

After running and breaking this bad code:

1. ✅ **Level 4 Complete**: Bad code baseline established
2. ⏳ **Level 5**: Learn TDD (write tests FIRST)
3. ⏳ **Level 6**: Refactor with SOLID principles
4. ⏳ **Level 7**: Write comprehensive tests
5. ⏳ **Level 8**: Run SonarQube and see improvements

---

**Time to Complete**: ~5 minutes  
**Learning**: Priceless  
**Motivation**: Maximum 🚀

---

## Quick Commands Reference

```bash
# Build
mvn clean install -DskipTests

# Run
mvn spring-boot:run

# Initialize data
curl -X POST http://localhost:8080/v1/init-data

# Transfer
curl -X POST http://localhost:8080/v1/transfers \
  -H "Content-Type: application/json" \
  -d '{"sourceUPI":"alice@okaxis","destinationUPI":"bob@paytm","amount":500}'

# Health check
curl http://localhost:8080/v1/health

# H2 Console
http://localhost:8080/h2-console
```

🎉 **You're running bad code!** Now let's make it better in Level 6!
