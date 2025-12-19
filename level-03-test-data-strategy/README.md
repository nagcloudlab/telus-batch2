# Level 3: Test Data Strategy

## What
Design and implement a comprehensive test data management strategy that provides consistent, versioned, and scenario-specific test data for all testing levels (unit, integration, performance, security).

## Why
- **Consistency**: Same test data across all environments
- **Repeatability**: Tests produce same results every time
- **Maintainability**: Centralized data management
- **Realistic Testing**: Production-like data without PII
- **Speed**: Pre-generated data for fast test execution
- **Coverage**: Data for all test scenarios from Level 1

## How
1. Define test data requirements for each scenario
2. Create data builders/factories for domain objects
3. Setup database seeding scripts (SQL/Flyway)
4. Implement data generation with Faker/Datafaker
5. Version test data with migration scripts
6. Create data sets for different test types
7. Implement data cleanup strategies
8. Document data relationships and constraints

## Success Metrics
- ✅ All 20 test scenarios from Level 1 have data
- ✅ Data generation automated
- ✅ Database seeding takes < 5 seconds
- ✅ Zero hardcoded test data in test classes
- ✅ Data versioned with schema migrations
- ✅ Production-like data available for performance tests

## Tools Used
- **Datafaker**: Realistic fake data generation
- **Flyway**: Database migration and versioning
- **Testcontainers**: Isolated database for tests
- **Liquibase** (alternative): Database change management
- **JSON/YAML**: Data fixtures for API tests

## Next Level
Level 4: Project Setup with Bad Code
