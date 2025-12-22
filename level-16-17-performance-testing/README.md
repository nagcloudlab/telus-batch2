# Levels 16-17: Performance Testing with JMeter DSL

## Overview
Comprehensive performance testing using JMeter DSL (Java-based, no GUI required).

### Level 16: Load & Stress Testing
- Baseline performance tests (100 users)
- Load testing (1000 concurrent users) 
- Stress testing (find breaking point)
- Performance SLA validation (<200ms p95)
- Response time percentiles (p50, p95, p99)

### Level 17: Advanced Performance Testing
- Spike testing (sudden traffic bursts)
- Soak testing (24-hour sustained load)
- Database connection pool tuning
- Memory leak detection
- Performance regression tests in CI

## Quick Start

### 1. Prerequisites
```bash
- Java 17+
- Maven 3.6+
- PostgreSQL 14+ (or use H2 for tests)
```

### 2. Run Application
```bash
mvn spring-boot:run
```

### 3. Run Performance Tests

**Level 16 - Baseline (100 users)**:
```bash
mvn test -Dtest=BaselinePerformanceTest
```

**Level 16 - Load Test (1000 users)**:
```bash
mvn test -Dtest=LoadTest
```

**Level 16 - Stress Test (find limits)**:
```bash
mvn test -Dtest=StressTest
```

**Level 17 - Spike Test**:
```bash
mvn test -Dtest=SpikeTest
```

**Level 17 - Soak Test** (long running):
```bash
mvn test -Dtest=SoakTest
```

## Performance Test Reports

After running tests, find reports in:
```
target/jmeter-reports/
├── index.html           # Dashboard
├── statistics.json      # Raw metrics
└── content/            # Detailed charts
```

## Key Performance Metrics

### SLA Targets (from Level 1)
- **P95 Response Time**: < 200ms
- **P99 Response Time**: < 500ms
- **Error Rate**: < 0.1%
- **Throughput**: > 1000 TPS

### Connection Pool Configuration
```yaml
hikari:
  maximum-pool-size: 20
  minimum-idle: 5
  connection-timeout: 30000
  leak-detection-threshold: 60000
```

## Test Scenarios

### Level 16
1. **Baseline** - 100 users, 60s, validates basic performance
2. **Load** - 1000 users, 5 min, tests sustained load
3. **Stress** - Ramp up to breaking point

### Level 17
1. **Spike** - 100 → 2000 → 100 users suddenly
2. **Soak** - 500 users for 24 hours
3. **Memory Leak** - Monitor heap over time

## JMeter DSL Benefits

✅ **Java-based** - No XML configuration
✅ **IDE support** - Auto-completion, debugging
✅ **Version control** - Easy to diff and review
✅ **CI/CD friendly** - Runs in Maven
✅ **Programmatic** - Dynamic test generation

## Example Test
```java
@Test
public void baselineTest() throws IOException {
    testPlan(
        threadGroup(100, 60,  // 100 users, 60 seconds
            httpSampler("http://localhost:8080/v1/transfers")
                .post("{\"sourceUPI\":\"alice@okaxis\",...}")
        ),
        htmlReporter("target/jmeter-reports")
    ).run();
}
```

## Monitoring

### Actuator Endpoints
- `http://localhost:8080/actuator/metrics`
- `http://localhost:8080/actuator/prometheus`
- `http://localhost:8080/actuator/health`

### Key Metrics
- `http_server_requests_seconds` - Response times
- `hikaricp_connections_active` - DB connections
- `jvm_memory_used_bytes` - Memory usage

## Troubleshooting

### High Response Times
1. Check database connection pool size
2. Review slow queries (enable SQL logging)
3. Check CPU and memory usage
4. Look for database locks

### Connection Pool Exhausted
```
HikariPool: Connection is not available
```
→ Increase `maximum-pool-size` or reduce load

### Memory Leaks
Monitor heap growth over time in soak tests.
Use heap dumps if memory grows continuously.

## Project Structure
```
src/
├── main/java/
│   └── com/npci/transfer/
│       ├── controller/     # REST APIs
│       ├── service/        # Business logic
│       └── entity/         # JPA entities
└── test/java/
    └── com/npci/transfer/performance/
        ├── level16/       # Load & stress tests
        └── level17/       # Advanced tests
```

## Next Steps
1. Run baseline test to establish performance metrics
2. Gradually increase load to find capacity
3. Identify and fix bottlenecks
4. Re-test to verify improvements
5. Set up continuous performance testing in CI

## Resources
- [JMeter DSL Documentation](https://abstracta.github.io/jmeter-java-dsl/)
- [HikariCP Configuration](https://github.com/brettwooldridge/HikariCP)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
