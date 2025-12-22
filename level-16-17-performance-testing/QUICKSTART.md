# Levels 16-17: Performance Testing - Quick Start

## 🚀 5-Minute Start

### Step 1: Start Application (Terminal 1)
```bash
mvn spring-boot:run
```

Wait for: `Started TransferServiceApplication`

### Step 2: Run Baseline Test (Terminal 2)
```bash
mvn test -Dtest=BaselinePerformanceTest
```

**Expected Output**:
```
100 users sending requests for 60 seconds...
✅ Test completed
📊 P95 response time: 150ms (Target: <200ms)
📊 Error rate: 0.0% (Target: <0.1%)
```

###Step 3: View Report
```bash
open target/jmeter-reports/index.html
```

---

## 📊 Understanding Results

### Good Performance ✅
```
Statistics:
  Total Requests: 45,000
  Errors: 0 (0%)
  P50: 80ms
  P95: 150ms  ← Under 200ms target ✅
  P99: 250ms
  Throughput: 750 TPS
```

### Poor Performance ❌
```
Statistics:
  Total Requests: 12,000
  Errors: 150 (1.25%)  ← Above 0.1% threshold ❌
  P50: 500ms
  P95: 2500ms  ← Way over 200ms target ❌
  P99: 5000ms
  Throughput: 200 TPS
```

---

## 🧪 Run All Level 16 Tests

```bash
# 1. Baseline (100 users, 1 min)
mvn test -Dtest=BaselinePerformanceTest

# 2. Load (1000 users, 5 min)
mvn test -Dtest=LoadTest

# 3. Stress (find breaking point)
mvn test -Dtest=StressTest
```

---

## 🔬 Run Level 17 Advanced Tests

```bash
# 1. Spike (sudden traffic burst)
mvn test -Dtest=SpikeTest

# 2. Soak (24-hour test - long!)
mvn test -Dtest=SoakTest -Dtest.duration=24h

# 3. Memory Leak Detection
mvn test -Dtest=MemoryLeakTest
```

---

## 📈 Key Metrics to Watch

### Response Time
- **P50** (Median): Typical user experience
- **P95**: 95% of users see this or better
- **P99**: Worst 1% of requests

**Target**: P95 < 200ms (from Level 1 requirement)

### Throughput
- **TPS** (Transactions Per Second)
- Higher = better
- **Target**: > 1000 TPS

### Error Rate
- Percentage of failed requests
- **Target**: < 0.1%

### Connection Pool
- Active connections
- Should stay below max pool size
- Watch for connection exhaustion

---

## 🔧 Quick Fixes

### Problem: High Response Times
```yaml
# Increase connection pool
spring:
  datasource:
    hikari:
      maximum-pool-size: 40  # Was 20
```

### Problem: Connection Exhausted
```
ERROR: Connection is not available
```

**Fix**: Increase max pool size OR reduce load

### Problem: Memory Keeps Growing
```bash
# Take heap dump
jmap -dump:live,format=b,file=heap.bin <PID>

# Analyze with VisualVM or MAT
```

---

## 📊 Reading the Dashboard Report

### Statistics Tab
- **Label**: Request name
- **# Samples**: Total requests
- **Average**: Average response time
- **90%/95%/99% Line**: Percentiles ← FOCUS HERE!
- **Error %**: Error rate
- **Throughput**: Requests/second

### Charts Tab
- **Response Times Over Time**: Look for trends
- **Active Threads**: Concurrent users
- **Transactions Per Second**: System capacity

### Look For:
- ✅ Response times stable over time
- ✅ Error rate near zero
- ✅ Throughput consistent
- ❌ Response times increasing (bad!)
- ❌ Errors spiking (bad!)
- ❌ Throughput dropping (bad!)

---

## 🎯 Performance Testing Checklist

### Before Testing
- [ ] Application running
- [ ] Database available
- [ ] Baseline metrics captured
- [ ] Monitoring enabled

### During Testing
- [ ] Watch CPU/memory usage
- [ ] Monitor database connections
- [ ] Check application logs
- [ ] Note any errors

### After Testing
- [ ] Review HTML report
- [ ] Check if SLA targets met
- [ ] Document findings
- [ ] Identify bottlenecks

---

## 🚦 Performance Test Progression

```
Week 1: Baseline
├─ Run baseline test
├─ Establish metrics
└─ Document current performance

Week 2: Load Testing
├─ Increase to 500 users
├─ Then 1000 users
└─ Find comfortable capacity

Week 3: Stress Testing
├─ Push to limits
├─ Find breaking point
└─ Identify bottlenecks

Week 4: Optimization
├─ Fix bottlenecks
├─ Re-test
└─ Verify improvements

Week 5: Advanced
├─ Spike testing
├─ Soak testing
└─ CI integration
```

---

## 💡 Pro Tips

1. **Always warm up** - First few requests are slower
2. **Run multiple times** - Results vary
3. **Test realistic scenarios** - Use actual usage patterns
4. **Monitor everything** - CPU, memory, DB, network
5. **Start small** - Don't jump to 10,000 users immediately
6. **Compare apples to apples** - Same environment, same data
7. **Document everything** - What changed, what improved

---

## 🆘 Common Issues

| Issue | Cause | Fix |
|-------|-------|-----|
| Cannot connect to app | App not running | Start with `mvn spring-boot:run` |
| Tests fail immediately | Wrong port/URL | Check `application.yml` |
| Very slow responses | DB not optimized | Add indexes, tune pool |
| Connection timeout | Pool too small | Increase `maximum-pool-size` |
| Out of memory | Memory leak | Run soak test, analyze heap |

---

## 📚 Next Steps

1. ✅ Run baseline test
2. Review HTML report
3. Compare against SLA targets
4. If failing: identify bottleneck
5. If passing: increase load
6. Iterate until optimal

**Happy Performance Testing!** 🚀
