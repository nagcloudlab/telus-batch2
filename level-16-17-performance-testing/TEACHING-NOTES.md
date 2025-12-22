# Levels 16-17: Performance Testing - Teaching Notes

## Session Overview (4-6 hours total)

### Level 16: Load & Stress Testing (2-3 hours)
- JMeter DSL fundamentals
- Baseline performance testing
- Load testing patterns
- Stress testing methodology
- Response time analysis (percentiles)
- Performance SLA validation

### Level 17: Advanced Performance Testing (2-3 hours)
- Spike testing
- Soak testing (long duration)
- Connection pool tuning
- Memory leak detection
- CI/CD integration
- Performance regression testing

---

## Part 1: Why Performance Testing? (30 min)

### The Production Horror Story

**Scenario**: E-commerce site launches sale

```
9:00 AM - Sale starts
9:01 AM - Site slow
9:02 AM - Site very slow  
9:03 AM - Site down ❌
9:30 AM - Lost ₹50 lakhs in sales
```

**What happened?**
- Expected: 1,000 concurrent users
- Actual: 10,000 concurrent users (10x)
- Database connections: Exhausted
- Response times: 30+ seconds
- Error rate: 60%

**Root Cause**: Never tested with realistic load!

---

### Performance Testing Benefits

1. **Find bottlenecks BEFORE production**
2. **Validate capacity planning**
3. **Ensure SLA compliance**
4. **Prevent revenue loss**
5. **Improve user experience**
6. **Support scaling decisions**

---

## Part 2: JMeter DSL vs Traditional JMeter (20 min)

### Traditional JMeter (GUI-based)

**Workflow**:
```
1. Open JMeter GUI
2. Add Thread Group manually
3. Add HTTP Sampler manually
4. Add Listeners manually
5. Save as .jmx XML file
6. Run from command line
```

**Problems**:
- ❌ XML is hard to read/maintain
- ❌ No IDE support (no auto-complete)
- ❌ Difficult to version control (XML diffs)
- ❌ No type safety
- ❌ Hard to debug
- ❌ Steep learning curve

---

### JMeter DSL (Java-based)

**Workflow**:
```java
@Test
public void loadTest() {
    testPlan(
        threadGroup(1000, 300,  // 1000 users, 5 min
            httpSampler("http://localhost:8080/api")
                .post(body)
        )
    ).run();
}
```

**Benefits**:
- ✅ Pure Java (IDE support)
- ✅ Auto-completion
- ✅ Type-safe
- ✅ Easy to debug
- ✅ Git-friendly
- ✅ Runs in Maven
- ✅ CI/CD ready

---

## Part 3: JMeter DSL Fundamentals (45 min)

### Core Concepts

#### 1. Test Plan
```java
testPlan(
    // Your test elements here
).run();
```

#### 2. Thread Group
```java
threadGroup(users, duration,
    // Your samplers here
)
```

**Parameters**:
- `users`: Number of concurrent users
- `duration`: How long to run (seconds)

#### 3. HTTP Sampler
```java
httpSampler("URL")
    .post(body)
    .header("Content-Type", "application/json")
```

#### 4. Assertions
```java
.children(
    responseAssertion()
        .containsSubstrings("SUCCESS")
)
```

#### 5. Listeners/Reporters
```java
htmlReporter("target/reports")
```

---

### Complete Example

```java
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;
import java.io.IOException;
import org.junit.jupiter.api.Test;

public class BaselineTest {
    
    @Test
    public void baseline100Users() throws IOException {
        testPlan(
            threadGroup(100, 60,  // 100 users, 60 seconds
                httpSampler("http://localhost:8080/v1/transfers")
                    .post("{"
                        + "\"sourceUPI\":\"alice@okaxis\","
                        + "\"destinationUPI\":\"bob@paytm\","
                        + "\"amount\":100.00"
                        + "}")
                    .header("Content-Type", "application/json")
                    .children(
                        responseAssertion()
                            .containsSubstrings("SUCCESS"),
                        durationAssertion(200)  // Max 200ms
                    )
            ),
            htmlReporter("target/jmeter-reports")
        ).run();
    }
}
```

---

## Part 4: Level 16 - Baseline Testing (30 min)

### Purpose
Establish baseline performance with **normal load**.

### Scenario
- **Users**: 100 concurrent
- **Duration**: 60 seconds  
- **Goal**: Establish baseline metrics

### Key Metrics to Capture

1. **Response Times**
   - P50 (median)
   - P95 (95th percentile) ← **Most important!**
   - P99 (99th percentile)

2. **Throughput**
   - Transactions Per Second (TPS)

3. **Error Rate**
   - Percentage of failed requests

4. **Resource Usage**
   - CPU utilization
   - Memory usage
   - Database connections

### SLA from Level 1

Remember our requirements from Level 1:
- **P95 response time**: < 200ms
- **Error rate**: < 0.1%
- **Availability**: 99.9%

### Sample Test
```java
@Test
public void baselineTest() throws IOException {
    TestPlanStats stats = testPlan(
        threadGroup(100, 60,
            httpSampler("http://localhost:8080/v1/transfers")
                .post(requestBody())
        ),
        htmlReporter("target/reports/baseline")
    ).run();
    
    // Validate SLA
    Duration p95 = stats.overall().sampleTimePercentile95();
    assertThat(p95).isLessThan(Duration.ofMillis(200));
}
```

---

## Part 5: Level 16 - Load Testing (45 min)

### Purpose
Test system under **sustained high load**.

### Scenario
- **Users**: 1000 concurrent
- **Duration**: 5 minutes (300 seconds)
- **Ramp-up**: 60 seconds
- **Goal**: Verify system handles expected peak load

### Ramp-Up Strategy

```
Users
1000 |                    ╔═══════════════════════╗
     |                ╔═══╝                      
     |            ╔═══╝
     |        ╔═══╝
     |    ╔═══╝
   0 |════╝
     └─────────────────────────────────────────→ Time
          60s              300s
      (ramp-up)         (sustained)
```

**Why Ramp-Up?**
- Gradually increases load
- Avoids shocking the system
- Mimics real-world traffic patterns
- Allows system to warm up

### Code Example
```java
@Test
public void loadTest1000Users() throws IOException {
    testPlan(
        threadGroup()
            .rampToAndHold(1000, Duration.ofSeconds(60), Duration.ofMinutes(5))
            .children(
                httpSampler("http://localhost:8080/v1/transfers")
                    .post(requestBody())
            ),
        htmlReporter("target/reports/load")
    ).run();
}
```

### What to Watch For

✅ **Good Signs**:
- Response times stable
- Error rate near zero
- CPU/memory stable
- No database connection issues

❌ **Warning Signs**:
- Response times increasing
- Errors appearing
- CPU/memory climbing
- Connection pool exhausted

---

## Part 6: Level 16 - Stress Testing (45 min)

### Purpose
Find the **breaking point** of the system.

### Strategy
Gradually increase load until system fails or degrades.

### Scenario
```
Users
5000 |                                        ╔═══╗
4000 |                                    ╔═══╝   ╚═══╗
3000 |                                ╔═══╝           ╚═══╗
2000 |                            ╔═══╝                   ╚═══╗
1000 |                        ╔═══╝                           ╚═══╗
   0 |════════════════════════╝                                   ╚═══
     └────────────────────────────────────────────────────────────────→
         Increase by 500 every 2 minutes until failure
```

### Code Example
```java
@Test
public void stressTest() throws IOException {
    testPlan(
        // Start with 100
        threadGroup(100, 120,
            httpSampler(url).post(body)
        ),
        // Add 500 more
        threadGroup(500, 120,
            httpSampler(url).post(body)
        ),
        // Add 1000 more
        threadGroup(1000, 120,
            httpSampler(url).post(body)
        ),
        // Continue until breaking point...
        htmlReporter("target/reports/stress")
    ).run();
}
```

### Finding the Breaking Point

**Indicators of Failure**:
1. **Response time > 5 seconds**
2. **Error rate > 5%**
3. **Throughput drops significantly**
4. **Application crashes**
5. **Database connections exhausted**

### Example Results
```
100 users  → P95:  80ms, Errors: 0%     ✅
500 users  → P95: 150ms, Errors: 0%     ✅
1000 users → P95: 200ms, Errors: 0.1%   ✅
2000 users → P95: 450ms, Errors: 1%     ⚠️
3000 users → P95: 2500ms, Errors: 15%   ❌ BREAKING POINT!
```

**Conclusion**: System capacity is ~2000 concurrent users

---

## Part 7: Response Time Percentiles (30 min)

### Why Percentiles Matter

**Problem with Averages**:
```
10 requests:
- 9 requests: 50ms
- 1 request: 5000ms

Average: 545ms  ← Misleading!
```

Most users experienced 50ms, but average says 545ms.

### Understanding Percentiles

**P50 (Median)**:
- 50% of requests faster than this
- Typical user experience

**P95** (95th percentile):
- 95% of requests faster than this
- Only 5% slower
- **Industry standard for SLAs**

**P99** (99th percentile):
- 99% of requests faster than this
- Only 1% slower
- Catches outliers

### Example Distribution
```
1000 requests:
  P50:  80ms  ← 50% under this
  P95: 200ms  ← 95% under this (SLA target!)
  P99: 500ms  ← 99% under this
  Max: 2000ms ← Worst case
```

### Code Example
```java
TestPlanStats stats = testPlan(...).run();

Duration p50 = stats.overall().sampleTimePercentile50();
Duration p95 = stats.overall().sampleTimePercentile95();
Duration p99 = stats.overall().sampleTimePercentile99();

System.out.println("P50: " + p50.toMillis() + "ms");
System.out.println("P95: " + p95.toMillis() + "ms");
System.out.println("P99: " + p99.toMillis() + "ms");

// Validate SLA
assertThat(p95.toMillis()).isLessThan(200);
```

---

## Part 8: Level 17 - Spike Testing (45 min)

### Purpose
Test system response to **sudden traffic bursts**.

### Real-World Scenario
- Normal: 100 users
- News goes viral / celebrity tweet
- **Suddenly**: 2000 users
- Then back to normal

### Traffic Pattern
```
Users
2000 |           ╔═══════╗
     |           ║       ║
     |           ║       ║
 100 |═══════════╝       ╚═══════════════
     └────────────────────────────────────→
        Steady  Spike!   Steady
```

### Code Example
```java
@Test
public void spikeTest() throws IOException {
    testPlan(
        // Normal load
        threadGroup(100, 60,
            httpSampler(url).post(body)
        ),
        // SPIKE! (happens at 60s mark)
        threadGroup()
            .rampTo(2000, Duration.ofSeconds(10))  // Rapid spike!
            .holdFor(Duration.ofSeconds(30))        // Hold for 30s
            .rampTo(0, Duration.ofSeconds(10)),    // Ramp down
        // Back to normal
        threadGroup(100, 60,
            httpSampler(url).post(body)
        ),
        htmlReporter("target/reports/spike")
    ).run();
}
```

### What to Check

✅ **Good System**:
- Handles spike without crashing
- Degrades gracefully
- Recovers quickly
- No lingering effects

❌ **Poor System**:
- Crashes during spike
- Takes long time to recover
- Errors persist after spike
- Memory doesn't return to normal

---

## Part 9: Level 17 - Soak Testing (45 min)

### Purpose
Test system under **sustained load over extended time**.

### Why Soak Testing?

Finds issues that only appear over time:
1. **Memory leaks**
2. **Connection leaks**
3. **Disk space issues**
4. **Log file growth**
5. **Cache issues**
6. **Resource exhaustion**

### Scenario
- **Users**: 500 concurrent
- **Duration**: 24 hours!
- **Goal**: No degradation over time

### Code Example
```java
@Test
public void soakTest24Hours() throws IOException {
    testPlan(
        threadGroup(500, Duration.ofHours(24),
            httpSampler(url).post(body)
        ),
        htmlReporter("target/reports/soak")
    ).run();
}
```

### Monitoring During Soak Test

**Every hour, check**:
1. Response times (should stay stable)
2. Memory usage (should not grow continuously)
3. Database connections (should stay stable)
4. CPU usage (should stay stable)
5. Error rate (should stay near zero)

### Red Flags
```
Hour  1: P95 = 150ms, Memory = 500MB  ✅
Hour  6: P95 = 180ms, Memory = 700MB  ⚠️
Hour 12: P95 = 350ms, Memory = 1.2GB  ❌ Memory leak!
Hour 18: P95 = 800ms, Memory = 1.8GB  ❌❌
Hour 24: CRASH - OutOfMemoryError     ❌❌❌
```

---

## Part 10: Connection Pool Tuning (45 min)

### The Problem

**Scenario**: 1000 concurrent users, but only 10 database connections!

```
Users: 1000 ──┐
              ├──► Queue! ──► DB Connections: 10 ❌
Users wait... │
```

**Result**: Slow responses, timeouts

### HikariCP Configuration

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20      # Max connections
      minimum-idle: 5             # Keep 5 ready
      connection-timeout: 30000   # Wait max 30s
      idle-timeout: 600000        # Close after 10 min idle
      max-lifetime: 1800000       # Recycle after 30 min
      leak-detection-threshold: 60000  # Warn if held >60s
```

### Sizing the Pool

**Formula** (rough):
```
pool_size = Tn × (Cm - 1) + 1

Where:
  Tn = Number of concurrent transactions
  Cm = Number of simultaneous connections per transaction
```

**Example**:
- 1000 concurrent users
- Each request uses 1 DB connection
- Pool size = 20-50 is reasonable

**Why not 1000?**
- Database can't handle 1000 connections!
- Most requests don't use DB entire time
- Connection pooling multiplexes

### Monitoring Pool

```java
@Autowired
HikariDataSource dataSource;

HikariPoolMXBean poolMBean = dataSource.getHikariPoolMXBean();

System.out.println("Active: " + poolMBean.getActiveConnections());
System.out.println("Idle: " + poolMBean.getIdleConnections());
System.out.println("Total: " + poolMBean.getTotalConnections());
System.out.println("Waiting: " + poolMBean.getThreadsAwaitingConnection());
```

### Tuning Process

1. **Start small** (10-20 connections)
2. **Run load test**
3. **Check metrics**:
   - Are threads waiting for connections?
   - Is pool exhausted?
4. **Increase if needed**
5. **Re-test**
6. **Repeat until optimal**

---

## Part 11: Memory Leak Detection (30 min)

### What is a Memory Leak?

**Definition**: Memory allocated but never released.

```java
// BAD - Memory leak!
public class LeakyService {
    private List<Transaction> cache = new ArrayList<>();
    
    public void process(Transaction tx) {
        cache.add(tx);  // Never removed!
        // Process...
    }
}
```

Over time:
```
Hour 1:  cache size = 1,000     (40MB)
Hour 2:  cache size = 2,000     (80MB)
Hour 6:  cache size = 10,000    (400MB)
Hour 24: cache size = 100,000   (4GB) ❌ OutOfMemoryError!
```

### Detecting Leaks in Soak Tests

**Signs**:
1. Memory usage grows continuously
2. Response times degrade over time
3. Eventually: OutOfMemoryError

**Monitoring**:
```java
Runtime runtime = Runtime.getRuntime();

long used = runtime.totalMemory() - runtime.freeMemory();
System.out.println("Memory used: " + (used / 1024 / 1024) + "MB");
```

**In Performance Test**:
```java
@Test
public void memoryLeakDetectionTest() {
    long startMemory = getUsedMemory();
    
    testPlan(
        threadGroup(100, 3600,  // 1 hour
            httpSampler(url).post(body)
        )
    ).run();
    
    long endMemory = getUsedMemory();
    long growth = endMemory - startMemory;
    
    // Memory should not grow more than 20%
    assertThat(growth).isLessThan(startMemory * 0.2);
}
```

---

## Part 12: CI/CD Integration (30 min)

### Why Performance Tests in CI?

1. **Catch regressions early**
2. **Every commit tested**
3. **Fail build if performance degrades**
4. **Track trends over time**

### Maven Configuration

```xml
<!-- Separate performance tests -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-failsafe-plugin</artifactId>
    <configuration>
        <includes>
            <include>**/performance/**/*Test.java</include>
        </includes>
    </configuration>
</plugin>
```

### Jenkins Pipeline

```groovy
pipeline {
    agent any
    
    stages {
        stage('Build') {
            steps {
                sh 'mvn clean install'
            }
        }
        
        stage('Performance Test') {
            steps {
                sh 'mvn failsafe:integration-test'
            }
        }
        
        stage('Performance Report') {
            steps {
                publishHTML([
                    reportDir: 'target/jmeter-reports',
                    reportFiles: 'index.html',
                    reportName: 'Performance Report'
                ])
            }
        }
    }
    
    post {
        failure {
            emailext(
                subject: "Performance degradation detected!",
                body: "P95 exceeded 200ms threshold"
            )
        }
    }
}
```

### Performance Gates

```java
@Test
public void performanceGate() {
    TestPlanStats stats = testPlan(...).run();
    
    Duration p95 = stats.overall().sampleTimePercentile95();
    double errorRate = stats.overall().errorsCount() / 
                       (double) stats.overall().samplesCount();
    
    // Fail build if SLA violated
    assertThat(p95.toMillis())
        .as("P95 response time")
        .isLessThan(200);
        
    assertThat(errorRate)
        .as("Error rate")
        .isLessThan(0.001);  // 0.1%
}
```

---

## Part 13: Key Takeaways

### Level 16 Summary

✅ **Baseline Testing**
- Establish normal performance metrics
- 100 users, 60 seconds
- Capture P50, P95, P99

✅ **Load Testing**
- Test sustained high load
- 1000 users, 5 minutes
- Ramp up gradually

✅ **Stress Testing**
- Find breaking point
- Gradually increase load
- Identify capacity limits

✅ **Percentiles**
- P95 is the key metric
- Use for SLA validation
- More reliable than average

### Level 17 Summary

✅ **Spike Testing**
- Sudden traffic bursts
- Test recovery
- Ensure no lingering effects

✅ **Soak Testing**
- 24-hour sustained load
- Find memory leaks
- Verify stability over time

✅ **Connection Pool Tuning**
- Size appropriately
- Monitor actively
- Tune based on metrics

✅ **CI/CD Integration**
- Automated performance testing
- Performance gates
- Fail fast on regressions

---

## Part 14: Best Practices

### DO ✅

1. **Start with baseline** - Know your starting point
2. **Gradually increase load** - Don't jump to max
3. **Use realistic scenarios** - Match production usage
4. **Monitor everything** - CPU, memory, DB, network
5. **Run multiple times** - Results vary
6. **Use percentiles, not averages** - P95 is key
7. **Test in prod-like environment** - Same config, same data
8. **Warm up first** - First requests are slower
9. **Document findings** - Track improvements
10. **Fail fast** - Stop if critical issues found

### DON'T ❌

1. ❌ **Don't test only happy path** - Include errors
2. ❌ **Don't ignore resource limits** - DB, CPU, memory
3. ❌ **Don't test on dev laptop** - Use proper environment
4. ❌ **Don't test without monitoring** - You'll miss issues
5. ❌ **Don't assume linear scaling** - 10x load ≠ 10x resources
6. ❌ **Don't skip warm-up** - Cold starts are slower
7. ❌ **Don't test empty database** - Use realistic data
8. ❌ **Don't only test peak load** - Test various levels
9. ❌ **Don't forget cleanup** - Reset between tests
10. ❌ **Don't ignore trends** - Watch for degradation

---

## Part 15: Troubleshooting Guide

### High Response Times

**Symptoms**: P95 > 500ms

**Possible Causes**:
1. Database queries slow
2. Connection pool too small
3. CPU maxed out
4. Memory issues
5. Network latency

**How to Fix**:
1. Enable SQL logging - find slow queries
2. Add database indexes
3. Increase connection pool
4. Scale horizontally
5. Optimize algorithms

---

### Connection Pool Exhausted

**Symptoms**:
```
Connection is not available, request timed out after 30000ms
```

**Cause**: More concurrent requests than connections

**Fix**:
```yaml
hikari:
  maximum-pool-size: 40  # Increase from 20
```

**Or**: Optimize queries to hold connections shorter

---

### Memory Leaks

**Symptoms**:
- Memory grows continuously
- Response times degrade over time
- Eventually: OutOfMemoryError

**How to Diagnose**:
1. Run soak test
2. Monitor heap usage
3. Take heap dump if growing
4. Analyze with VisualVM/MAT

**Common Causes**:
- Collections never cleared
- Static collections
- Listeners not removed
- Unclosed resources

---

### Database Locks

**Symptoms**:
- Some requests very slow
- Random timeouts
- Database deadlocks

**How to Diagnose**:
```sql
-- PostgreSQL
SELECT * FROM pg_stat_activity 
WHERE state = 'active';

SELECT * FROM pg_locks;
```

**Fix**:
- Optimize transaction scope
- Reduce lock time
- Use appropriate isolation level

---

## Part 16: Hands-On Exercises

### Exercise 1: Run Baseline Test (15 min)
1. Start application
2. Run BaselinePerformanceTest
3. Review HTML report
4. Note P50, P95, P99 values

### Exercise 2: Cause Performance Degradation (20 min)
1. Add `Thread.sleep(100)` to transfer service
2. Run baseline test again
3. Compare results
4. See how P95 changed

### Exercise 3: Fix Connection Pool Issue (30 min)
1. Set `maximum-pool-size: 2`
2. Run load test with 100 users
3. Observe connection timeout errors
4. Increase pool size
5. Re-test and verify fix

### Exercise 4: Create Custom Load Test (30 min)
1. Create test with 500 users
2. Add custom assertions
3. Generate HTML report
4. Analyze results

### Exercise 5: Detect Memory Leak (30 min)
1. Add memory leak to code:
```java
List<byte[]> leak = new ArrayList<>();
leak.add(new byte[1024 * 1024]);  // 1MB each
```
2. Run soak test (shorter, 10 min)
3. Monitor memory growth
4. Document findings

---

## Part 17: Real-World Case Study

### Company: NPCI UPI Service

**Challenge**:
- Handle Diwali shopping surge
- Expected: 10x normal traffic
- Cannot fail (₹1000 crores at stake)

**Approach**:
1. **Week 1**: Baseline testing
   - Normal load: 5,000 TPS
   - P95: 120ms

2. **Week 2**: Load testing
   - Tested 50,000 TPS
   - P95: 350ms ❌ (exceeds 200ms SLA)

3. **Week 3**: Optimization
   - Added database read replicas
   - Increased connection pool
   - Optimized queries

4. **Week 4**: Re-test
   - 50,000 TPS
   - P95: 180ms ✅

5. **Week 5**: Stress & Soak
   - Tested 100,000 TPS
   - 48-hour soak test
   - No issues ✅

**Diwali Result**:
- Peak: 75,000 TPS
- P95: 165ms
- Error rate: 0.02%
- **Success!** ✅

---

## Summary

### Skills Mastered

**Level 16**:
- ✅ JMeter DSL fundamentals
- ✅ Baseline testing
- ✅ Load testing (1000 users)
- ✅ Stress testing (find limits)
- ✅ Response time analysis
- ✅ SLA validation

**Level 17**:
- ✅ Spike testing
- ✅ Soak testing (24 hours)
- ✅ Connection pool tuning
- ✅ Memory leak detection
- ✅ CI/CD integration
- ✅ Performance regression testing

### Production-Ready Checklist

- [ ] Baseline metrics established
- [ ] Load tests passing at expected peak
- [ ] Stress limits documented
- [ ] Spike recovery verified
- [ ] 24-hour soak test passed
- [ ] Connection pool optimized
- [ ] No memory leaks detected
- [ ] Performance tests in CI/CD
- [ ] Monitoring in place
- [ ] Runbooks created

**Congratulations!** You can now confidently performance test production systems! 🚀

---

## Resources

- [JMeter DSL Documentation](https://abstracta.github.io/jmeter-java-dsl/)
- [HikariCP Best Practices](https://github.com/brettwooldridge/HikariCP/wiki)
- [Spring Boot Actuator Metrics](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html#actuator.metrics)
- [VisualVM (Heap Analysis)](https://visualvm.github.io/)
- [Performance Testing Best Practices](https://www.nginx.com/blog/performance-testing/)

---

**End of Teaching Notes - Levels 16-17** 🎓
