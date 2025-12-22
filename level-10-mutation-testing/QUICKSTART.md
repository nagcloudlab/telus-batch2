# 🚀 Quick Start - Level 10: Mutation Testing

## 3 Commands to Success

```bash
# 1. Run tests (verify they pass)
mvn clean test

# 2. Run mutation testing  
mvn test pitest:mutationCoverage

# 3. View results
open target/pit-reports/$(ls target/pit-reports | head -1)/index.html
```

---

## Expected Results

```
>> Generated 247 mutations
>> Killed 215 mutants (87%)
>> Survived 32 mutants (13%)
>> Mutation Coverage: 87% ✅ (Target: 80%+)
```

---

## What to Do Next

1. **Click on a red line** in the report
2. **See which mutation survived**
3. **Write a test to kill it**
4. **Re-run PIT**
5. **Repeat until 80%+ mutation score**

**Happy Mutant Hunting!** 🧬
