# 🧬 Mutation Testing Complete Guide

## What is Mutation Testing?

**Concept**: Intentionally break your code to test if your tests catch it!

```
Original:  if (balance >= amount)
Mutated:   if (balance > amount)  ← Changed!

Test fails? ✅ KILLED (good test!)
Test passes? ❌ SURVIVED (weak test - fix it!)
```

---

## Why Mutation Testing?

### The Problem
```
"We have 96% code coverage!"
→ But do tests actually VERIFY behavior?
→ Or just EXECUTE code without checking?
```

### The Solution
```
Code Coverage:     "Did we run the code?"
Mutation Testing:  "Did we verify it works correctly?"
```

---

## Common Mutation Types

| Type | Example | How to Kill |
|------|---------|-------------|
| **Boundary** | `>=` → `>` | Test exact boundary |
| **Return** | `return true` → `return false` | Assert return value |
| **Math** | `+` → `-` | Verify calculation result |
| **Negate** | `!x` → `x` | Test both true/false |
| **Increment** | `i++` → `i--` | Verify loop behavior |

---

## Running PIT

### Basic Command
```bash
mvn test pitest:mutationCoverage
```

### View Report
```bash
open target/pit-reports/YYYYMMDDHHMMSS/index.html
```

### Output Explained
```
Generated 247 mutations    ← Total mutations created
Killed 215 (87%)           ← Tests caught 215
Survived 32 (13%)          ← Tests missed 32
Mutation Coverage: 87%     ← Your score!
```

---

## Improving Mutation Score

### Strategy 1: Add Assertions

**Bad** ❌:
```java
@Test
void testTransfer() {
    service.transfer(request);
    // No assertions!
}
```

**Good** ✅:
```java
@Test
void shouldUpdateBalances() {
    service.transfer(request);
    assertEquals(expectedBalance, account.getBalance());
}
```

---

### Strategy 2: Test Boundaries

**For every condition**, test: **below**, **at**, and **above**

```java
// Code: if (balance >= MIN_BALANCE)

@Test
void shouldRejectBelowMinimum() {
    balance = MIN - 1;  // Below
}

@Test
void shouldAllowExactlyMinimum() {
    balance = MIN;      // At ← Often forgotten!
}

@Test
void shouldAllowAboveMinimum() {
    balance = MIN + 1;  // Above
}
```

---

### Strategy 3: Assert Return Values

**Bad** ❌:
```java
service.validate(request);
```

**Good** ✅:
```java
assertTrue(service.validate(request));
assertFalse(service.validate(invalidRequest));
```

---

### Strategy 4: Verify State Changes

**Bad** ❌:
```java
service.transfer(request);
verify(repository).save(any());
```

**Good** ✅:
```java
service.transfer(request);
assertEquals(sourceInitial - amount, source.getBalance());
assertEquals(destInitial + amount, dest.getBalance());
```

---

## PIT Configuration

### Minimal Setup
```xml
<plugin>
    <groupId>org.pitest</groupId>
    <artifactId>pitest-maven</artifactId>
    <version>1.15.3</version>
    <configuration>
        <targetClasses>
            <param>com.npci.transfer.service.*</param>
        </targetClasses>
        <mutationThreshold>80</mutationThreshold>
    </configuration>
</plugin>
```

### Performance Tuning
```xml
<threads>4</threads>              ← Use 4 CPU cores
<timeoutConstant>8000</timeoutConstant>
<withHistory>true</withHistory>   ← Incremental analysis
```

---

## Mutation Score Goals

| Score | Rating | Action |
|-------|--------|--------|
| 90-100% | Excellent ⭐⭐⭐⭐⭐ | Maintain |
| 80-89% | Good ⭐⭐⭐⭐ | Production ready |
| 70-79% | Fair ⭐⭐⭐ | Improve tests |
| < 70% | Poor ⭐⭐ | Major gaps |

**Target**: 80-90% (sweet spot)

---

## Common Issues

### PIT Takes Too Long
```xml
<!-- Reduce scope -->
<targetClasses>
    <param>com.npci.transfer.service.TransferService</param>
</targetClasses>

<!-- Use history -->
<withHistory>true</withHistory>
```

### Out of Memory
```bash
export MAVEN_OPTS="-Xmx2048m"
```

### Build Fails (Score < Threshold)
```
→ Good! Means tests need improvement
→ Fix tests, don't lower threshold
```

---

## CI/CD Integration

```yaml
- name: Run Tests
  run: mvn clean test

- name: Mutation Testing
  run: mvn pitest:mutationCoverage
  
- name: Check Threshold
  run: |
    if [ mutation_score < 80 ]; then
      echo "Mutation score too low!"
      exit 1
    fi
```

---

## Best Practices

1. ✅ **Run PIT regularly** (weekly minimum, every PR ideally)
2. ✅ **Focus on business logic** (service layer, not DTOs)
3. ✅ **Test boundaries explicitly** (=, <, >, <=, >=)
4. ✅ **Always assert** (return values, state changes)
5. ✅ **Accept some survivors** (equivalent mutants exist)
6. ✅ **Use with coverage** (both metrics together)

---

## Key Takeaways

```
✅ Coverage ≠ Test Quality
✅ Mutation testing = Testing the tests
✅ 80%+ mutation score = Good tests
✅ PIT finds gaps in test logic
✅ Focus on boundaries and assertions
✅ Integrate into CI/CD pipeline
```

---

**Master mutation testing, master test quality!** 🧬
