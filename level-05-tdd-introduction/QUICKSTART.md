# Level 5: Quick Start Guide

## 🚀 Learn TDD in 30 Minutes

This guide helps you practice Test-Driven Development by adding a new feature to the transfer service.

---

## Prerequisites

```bash
# Check installations
java --version   # Java 17+
mvn --version    # Maven 3.8+

# Have Level 4 project ready
cd level-04-project-setup-bad-code
```

---

## Step 1: Add Test Dependencies (2 minutes)

Add to `pom.xml`:

```xml
<dependencies>
    <!-- Existing dependencies... -->
    
    <!-- JUnit 5 -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- AssertJ (better assertions) -->
    <dependency>
        <groupId>org.assertj</groupId>
        <artifactId>assertj-core</artifactId>
        <version>3.24.2</version>
        <scope>test</scope>
    </dependency>
    
    <!-- Mockito (for mocking) -->
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-core</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

## Step 2: Your First TDD Cycle (10 minutes)

### 🔴 RED: Write a Failing Test

Create `src/test/java/com/npci/transfer/TransferValidatorTest.java`:

```java
package com.npci.transfer;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;

class TransferValidatorTest {
    
    @Test
    void shouldRejectAmountBelowMinimum() {
        // Arrange
        TransferValidator validator = new TransferValidator();
        BigDecimal amount = new BigDecimal("0.50");
        
        // Act & Assert
        assertThatThrownBy(() -> validator.validateAmount(amount))
            .isInstanceOf(InvalidAmountException.class)
            .hasMessage("Minimum transfer amount is ₹1");
    }
}
```

**Run test**:
```bash
mvn test -Dtest=TransferValidatorTest

# Expected: ❌ Compilation error (classes don't exist)
```

---

### 🟢 GREEN: Make Test Pass

**Step 1**: Create exception class

`src/main/java/com/npci/transfer/InvalidAmountException.java`:
```java
package com.npci.transfer;

public class InvalidAmountException extends RuntimeException {
    public InvalidAmountException(String message) {
        super(message);
    }
}
```

**Step 2**: Create validator class

`src/main/java/com/npci/transfer/TransferValidator.java`:
```java
package com.npci.transfer;

import java.math.BigDecimal;

public class TransferValidator {
    
    private static final BigDecimal MIN_AMOUNT = new BigDecimal("1");
    
    public void validateAmount(BigDecimal amount) {
        if (amount.compareTo(MIN_AMOUNT) < 0) {
            throw new InvalidAmountException("Minimum transfer amount is ₹1");
        }
    }
}
```

**Run test**:
```bash
mvn test -Dtest=TransferValidatorTest

# Expected: ✅ Test passes!
```

---

### 🔵 REFACTOR: Improve (if needed)

Code is clean, no refactoring needed yet!

**Commit**:
```bash
git add .
git commit -m "Add minimum amount validation with TDD"
```

---

## Step 3: Add Maximum Validation (5 minutes)

### 🔴 RED: Write Test

Add to `TransferValidatorTest.java`:

```java
@Test
void shouldRejectAmountExceedingMaximum() {
    // Arrange
    TransferValidator validator = new TransferValidator();
    BigDecimal amount = new BigDecimal("150000");
    
    // Act & Assert
    assertThatThrownBy(() -> validator.validateAmount(amount))
        .isInstanceOf(InvalidAmountException.class)
        .hasMessage("Maximum per-transaction limit is ₹1,00,000");
}
```

**Run**: `mvn test` → ❌ Fails

---

### 🟢 GREEN: Make Pass

Update `TransferValidator.java`:

```java
public class TransferValidator {
    
    private static final BigDecimal MIN_AMOUNT = new BigDecimal("1");
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("100000");
    
    public void validateAmount(BigDecimal amount) {
        if (amount.compareTo(MIN_AMOUNT) < 0) {
            throw new InvalidAmountException("Minimum transfer amount is ₹1");
        }
        
        if (amount.compareTo(MAX_AMOUNT) > 0) {
            throw new InvalidAmountException("Maximum per-transaction limit is ₹1,00,000");
        }
    }
}
```

**Run**: `mvn test` → ✅ Both tests pass!

---

### 🔵 REFACTOR: Extract Methods

```java
public void validateAmount(BigDecimal amount) {
    validateMinimumAmount(amount);
    validateMaximumAmount(amount);
}

private void validateMinimumAmount(BigDecimal amount) {
    if (amount.compareTo(MIN_AMOUNT) < 0) {
        throw new InvalidAmountException("Minimum transfer amount is ₹1");
    }
}

private void validateMaximumAmount(BigDecimal amount) {
    if (amount.compareTo(MAX_AMOUNT) > 0) {
        throw new InvalidAmountException("Maximum per-transaction limit is ₹1,00,000");
    }
}
```

**Run**: `mvn test` → ✅ Still passes!

**Commit**:
```bash
git commit -am "Add maximum amount validation"
```

---

## Step 4: Add Edge Cases (5 minutes)

Add to `TransferValidatorTest.java`:

```java
@Test
void shouldAcceptMinimumAmount() {
    // Arrange
    TransferValidator validator = new TransferValidator();
    BigDecimal amount = new BigDecimal("1");
    
    // Act & Assert
    assertThatCode(() -> validator.validateAmount(amount))
        .doesNotThrowAnyException();
}

@Test
void shouldAcceptMaximumAmount() {
    // Arrange
    TransferValidator validator = new TransferValidator();
    BigDecimal amount = new BigDecimal("100000");
    
    // Act & Assert
    assertThatCode(() -> validator.validateAmount(amount))
        .doesNotThrowAnyException();
}

@Test
void shouldRejectNullAmount() {
    // Arrange
    TransferValidator validator = new TransferValidator();
    
    // Act & Assert
    assertThatThrownBy(() -> validator.validateAmount(null))
        .isInstanceOf(InvalidAmountException.class)
        .hasMessage("Amount is required");
}
```

**Run**: `mvn test` → ❌ Null test fails

**Fix**:
```java
public void validateAmount(BigDecimal amount) {
    if (amount == null) {
        throw new InvalidAmountException("Amount is required");
    }
    validateMinimumAmount(amount);
    validateMaximumAmount(amount);
}
```

**Run**: `mvn test` → ✅ All 5 tests pass!

---

## Step 5: Check Coverage (3 minutes)

Add JaCoCo plugin to `pom.xml`:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.jacoco</groupId>
            <artifactId>jacoco-maven-plugin</artifactId>
            <version>0.8.11</version>
            <executions>
                <execution>
                    <goals>
                        <goal>prepare-agent</goal>
                    </goals>
                </execution>
                <execution>
                    <id>report</id>
                    <phase>test</phase>
                    <goals>
                        <goal>report</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

**Generate coverage report**:
```bash
mvn clean test jacoco:report

# Open coverage report
open target/site/jacoco/index.html
```

**Expected Coverage**: 100% on TransferValidator! 🎉

---

## Step 6: Practice Complete Feature (5 minutes)

Add UPI validation using TDD:

```java
@Test
void shouldRejectSameSourceAndDestination() {
    // Arrange
    TransferValidator validator = new TransferValidator();
    
    // Act & Assert
    assertThatThrownBy(() -> 
        validator.validateUpiIds("alice@okaxis", "alice@okaxis"))
        .isInstanceOf(InvalidUpiException.class)
        .hasMessage("Cannot transfer to the same account");
}
```

**Your turn**: 
1. Watch test fail (RED)
2. Create InvalidUpiException
3. Implement validateUpiIds()
4. Watch test pass (GREEN)
5. Refactor if needed

---

## Common Commands

### Run All Tests
```bash
mvn test
```

### Run Specific Test
```bash
mvn test -Dtest=TransferValidatorTest
```

### Run Single Test Method
```bash
mvn test -Dtest=TransferValidatorTest#shouldRejectAmountBelowMinimum
```

### Run with Coverage
```bash
mvn clean test jacoco:report
```

### Watch Mode (Continuous Testing)
```bash
# Install: brew install watchexec (Mac) or equivalent
watchexec -e java mvn test
```

---

## TDD Checklist

For each new feature:

### 🔴 RED Phase
- [ ] Write test that fails
- [ ] Run test to confirm it fails
- [ ] Failure is for the RIGHT reason

### 🟢 GREEN Phase
- [ ] Write minimum code to pass
- [ ] Run test to confirm it passes
- [ ] No extra features added

### 🔵 REFACTOR Phase
- [ ] Clean up code
- [ ] Extract methods/constants
- [ ] Run tests to ensure still passing
- [ ] Commit

---

## Troubleshooting

### Test won't compile
```bash
# Clean and rebuild
mvn clean compile

# Check Java version
java --version  # Must be 17+
```

### Test passes when it should fail
```bash
# Check your assertion
# Make sure you're testing the right thing
assertThatThrownBy(() -> validator.validateAmount(amount))
    .isInstanceOf(InvalidAmountException.class);  // ✅ Good

assertThat(validator.validateAmount(amount))
    .isInstanceOf(InvalidAmountException.class);  // ❌ Wrong!
```

### Coverage report not generated
```bash
# Ensure JaCoCo plugin is in pom.xml
# Run with clean
mvn clean test jacoco:report
```

---

## Exercise: Practice TDD (20 minutes)

### Task
Add these validations using TDD:

1. **Empty UPI validation**
   - Write test for empty source UPI
   - Implement validation
   - Write test for empty destination UPI

2. **Null UPI validation**
   - Write test for null source UPI
   - Implement validation
   - Write test for null destination UPI

3. **Parameterized tests**
   - Test multiple valid amounts
   - Test multiple invalid amounts

**Goal**: Write tests FIRST, then implement!

---

## Expected Results

After completing this quickstart:

### Tests Written
- ✅ 5+ tests for amount validation
- ✅ 3+ tests for UPI validation
- ✅ 100% code coverage

### Skills Gained
- ✅ Red-Green-Refactor cycle
- ✅ Test-first mindset
- ✅ Edge case thinking
- ✅ Confidence in code

### Time Spent
- ✅ Setup: 2 min
- ✅ First TDD cycle: 10 min
- ✅ Additional features: 13 min
- ✅ Coverage check: 3 min
- ✅ **Total**: 28 minutes

---

## What You Learned

### TDD Principles
1. ✅ Tests drive design
2. ✅ Write minimum code
3. ✅ Refactor with safety
4. ✅ Fast feedback loop

### Benefits Experienced
- 😎 Confidence in code
- 🚀 Faster development
- 🐛 Zero bugs
- 📚 Tests as documentation

### Next Steps
- ⏳ Apply TDD to Level 4 bad code
- ⏳ Refactor with SOLID (Level 6)
- ⏳ Add integration tests (Level 12)

---

## Quick Reference

### TDD Cycle
```
1. 🔴 Write failing test
2. 🟢 Make it pass (minimum code)
3. 🔵 Refactor
4. Repeat
```

### Test Structure
```java
@Test
void shouldDescribeWhatItTests() {
    // Arrange - Setup
    // Act - Execute
    // Assert - Verify
}
```

### Assertions
```java
// Exception testing
assertThatThrownBy(() -> code())
    .isInstanceOf(Exception.class)
    .hasMessage("message");

// No exception testing
assertThatCode(() -> code())
    .doesNotThrowAnyException();

// Value testing
assertThat(actual).isEqualTo(expected);
```

---

**Time to Complete**: ~30 minutes  
**Difficulty**: Medium  
**Impact**: High 🚀

🎉 **You're now practicing TDD!** Keep the cycle going!
