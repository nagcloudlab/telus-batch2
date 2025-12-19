# Level 4: Project Setup with Bad Code

## What
Create a working Spring Boot transfer-service with **intentionally bad code** to establish a baseline for improvement. This demonstrates why shift-left testing, SOLID principles, and refactoring are essential.

## Why
- **Learning by Contrast**: Shows the problems of poor code quality
- **Baseline Metrics**: Establishes measurable starting point
- **Realistic Scenario**: Simulates legacy code situations
- **Motivation**: Makes refactoring benefits tangible
- **Before/After Comparison**: Quantifies improvements

## How
1. Initialize Spring Boot project with Maven
2. Create monolithic controller with ALL business logic
3. Use hardcoded values and tight coupling
4. No separation of concerns (no services, repositories)
5. No tests (yet - that's the point!)
6. No error handling or validation
7. Database operations directly in controller
8. Zero documentation

## Success Metrics
- ✅ Application starts and runs
- ✅ Transfer endpoint works (barely)
- ✅ Code smells intentionally present
- ✅ SonarQube shows critical issues
- ✅ Zero test coverage
- ✅ High cyclomatic complexity

## Bad Code Characteristics
❌ **God Object**: Controller does everything  
❌ **Tight Coupling**: Direct database access in controller  
❌ **Hardcoded Values**: Magic numbers everywhere  
❌ **No Validation**: Accepts any input  
❌ **No Error Handling**: Exceptions crash the app  
❌ **Long Methods**: 100+ lines per method  
❌ **No Tests**: Zero coverage  
❌ **Poor Naming**: Variables like `amt`, `src`, `dst`  

## Tools Used
- **Spring Boot 3.2**: Web framework
- **H2 Database**: In-memory database (for speed)
- **Maven**: Build tool
- **SonarQube**: Code quality analysis

## Next Level
Level 5: TDD Introduction - Write Tests FIRST
