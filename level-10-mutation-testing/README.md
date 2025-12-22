# 🧬 Level 10: Mutation Testing with PIT

**Testing Your Tests!**

## 📋 Overview

**Previous Level**: Level 9 - Security Analysis (96% coverage, 0 security bugs)  
**This Level**: Mutation Testing - Verify test quality  
**Next Level**: Level 11 - Performance Testing  

### The Problem
```
"We have 96% code coverage!"
→ But are the tests actually GOOD?
→ Do they catch real bugs?
→ Or just execute code without verifying behavior?
```

### The Solution: Mutation Testing

**PIT (Pitest)** mutates your code and checks if tests catch the mutations.

```
Original Code:     if (balance >= amount)
Mutated Code:      if (balance > amount)  ← Changed!

Test catches it?   ✅ Good test!
Test doesn't?      ❌ Weak test - needs improvement
```

---

## 🚀 Quick Start

```bash
# 1. Extract and navigate
cd level-10-mutation-testing

# 2. Run tests (verify they pass)
mvn clean test

# 3. Run mutation testing
mvn test pitest:mutationCoverage

# 4. View report
open target/pit-reports/$(ls target/pit-reports)/index.html
```

---

## 📊 Expected Results

```
================================================================================
- Statistics
================================================================================
>> Generated 247 mutations
>> Killed 215 mutants ✅
>> Survived 32 mutants
>> Mutation Coverage: 87%  ← Target: 80%+
```

---

## 🎯 Learning Objectives

1. ✅ Understand mutation testing
2. ✅ Configure PIT in Maven
3. ✅ Run mutation analysis
4. ✅ Interpret reports
5. ✅ Improve test quality
6. ✅ Achieve 80%+ mutation score

---

For complete documentation, see package contents.

*Level 10 - Mutation Testing - Test Your Tests!* 🧬
