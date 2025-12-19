# Level 5: TDD Introduction - Write Tests FIRST

## What
Learn Test-Driven Development (TDD) by writing tests BEFORE implementation. Use TDD to add a new feature (transaction limit validation) to the bad code from Level 4.

## Why
- **Design-First Thinking**: Tests drive better design
- **Confidence**: Know your code works from day one
- **Regression Prevention**: Tests catch future breaks
- **Documentation**: Tests show how to use the code
- **Refactoring Safety**: Change code without fear
- **Bug Prevention**: Catch issues before production

## How
1. Learn the Red-Green-Refactor cycle
2. Write a failing test (RED)
3. Write minimum code to pass (GREEN)
4. Improve the code (REFACTOR)
5. Repeat for each requirement
6. Apply TDD to add transaction limit feature
7. Experience the benefits firsthand

## Success Metrics
- ✅ Understand TDD cycle (Red → Green → Refactor)
- ✅ Write tests before implementation
- ✅ Add new feature using TDD
- ✅ Achieve >80% test coverage for new feature
- ✅ Experience faster debugging
- ✅ Feel more confident in code

## TDD Cycle

```
1. RED: Write a failing test
   ↓
2. GREEN: Write minimum code to pass
   ↓
3. REFACTOR: Improve code quality
   ↓
4. Repeat
```

## New Feature to Add (Using TDD)

**Feature**: Transaction Amount Limits Validation

**Requirements**:
- Minimum transfer: ₹1
- Maximum per transaction: ₹1,00,000
- Reject amounts outside range
- Clear error messages

## Tools Used
- **JUnit 5**: Test framework
- **AssertJ**: Fluent assertions
- **Mockito**: Mocking framework
- **Maven Surefire**: Test execution

## Next Level
Level 6: Refactoring with SOLID Principles
