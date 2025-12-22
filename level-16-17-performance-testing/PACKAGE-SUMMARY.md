# Levels 16-17: Performance Testing - Complete Package

## 📦 What's Included

### Documentation (5 files)
1. ✅ **README.md** - Complete overview and setup
2. ✅ **QUICKSTART.md** - 5-minute quick start guide
3. ✅ **TEACHING-NOTES.md** - Comprehensive teaching guide (17 parts)
4. ✅ **PACKAGE-SUMMARY.md** - This file
5. ✅ **pom.xml** - Maven configuration with JMeter DSL

### Sample Performance Tests (2 files)
1. ✅ **BaselinePerformanceTest.java** - 100 users, 60 seconds
2. ✅ **LoadTest.java** - 1000 users, 5 minutes

### Application Code (Complete Spring Boot app)
- ✅ Full working transfer service
- ✅ PostgreSQL/H2 support
- ✅ HikariCP connection pool configured
- ✅ Actuator metrics enabled
- ✅ All tests from previous levels (116 tests)

---

## 🎯 Learning Path

### Step 1: Read QUICKSTART.md (5 minutes)
Get up and running immediately with baseline test.

### Step 2: Review Sample Tests (15 minutes)
- Open `BaselinePerformanceTest.java`
- Understand JMeter DSL syntax
- See how assertions work
- Review `LoadTest.java` for ramp-up pattern

### Step 3: Study TEACHING-NOTES.md (2-4 hours)
Comprehensive guide covering:
- Part 1-3: Why performance testing, JMeter DSL fundamentals
- Part 4-7: Level 16 - Baseline, load, stress, percentiles
- Part 8-12: Level 17 - Spike, soak, connection pool, CI/CD
- Part 13-17: Takeaways, best practices, troubleshooting

### Step 4: Run Tests (30 minutes)
```bash
# Start app
mvn spring-boot:run

# In another terminal
mvn test -Dtest=BaselinePerformanceTest
mvn test -Dtest=LoadTest
```

### Step 5: Create Your Own Tests (1-2 hours)
- Create StressTest.java
- Create SpikeTest.java
- Create SoakTest.java

---

## 📚 Teaching Materials Breakdown

### TEACHING-NOTES.md (17 Parts)

**Fundamentals (Parts 1-3)**:
- Why performance testing?
- JMeter DSL vs traditional JMeter
- JMeter DSL fundamentals

**Level 16 (Parts 4-7)**:
- Baseline testing (100 users)
- Load testing (1000 users)
- Stress testing (find breaking point)
- Response time percentiles (P50, P95, P99)

**Level 17 (Parts 8-12)**:
- Spike testing (sudden bursts)
- Soak testing (24 hours)
- Connection pool tuning
- Memory leak detection
- CI/CD integration

**Practical (Parts 13-17)**:
- Key takeaways
- Best practices
- Troubleshooting guide
- Hands-on exercises
- Real-world case study

---

## 🎓 Course Structure Recommendation

### Session 1: Fundamentals (1.5 hours)
- Why performance testing? (30 min)
- JMeter DSL overview (30 min)
- Run baseline test (30 min)

**Hands-on**: Run BaselinePerformanceTest

---

### Session 2: Level 16 - Load & Stress (2 hours)
- Baseline testing concepts (30 min)
- Load testing patterns (45 min)
- Stress testing methodology (30 min)
- Percentiles deep-dive (15 min)

**Hands-on**: 
- Run LoadTest
- Create custom load test
- Exercise: Add sleep to cause degradation

---

### Session 3: Level 17 - Advanced (2 hours)
- Spike testing (30 min)
- Soak testing (30 min)
- Connection pool tuning (45 min)
- Memory leak detection (15 min)

**Hands-on**:
- Create spike test
- Tune connection pool
- Exercise: Detect memory leak

---

### Session 4: Production Readiness (1.5 hours)
- CI/CD integration (30 min)
- Performance gates (30 min)
- Best practices (15 min)
- Q&A (15 min)

**Hands-on**:
- Add performance test to Jenkins
- Create performance gate

---

## ✅ Tests You Can Create

Using the patterns from the provided samples:

### Level 16
1. ✅ **BaselinePerformanceTest** (Provided)
   - 100 users, 60s
   - Validates SLA

2. ✅ **LoadTest** (Provided)
   - 1000 users, 5 min
   - Sustained load

3. ⚪ **StressTest** (You create)
   - Ramp from 100 to 5000
   - Find breaking point

### Level 17
4. ⚪ **SpikeTest** (You create)
   - 100 → 2000 → 100
   - Test recovery

5. ⚪ **SoakTest** (You create)
   - 500 users, 24 hours
   - Detect memory leaks

6. ⚪ **MemoryLeakTest** (You create)
   - Monitor heap over time
   - Assert memory stable

---

## 🔧 Quick Reference

### Run Tests
```bash
# Baseline (100 users, 1 min)
mvn test -Dtest=BaselinePerformanceTest

# Load (1000 users, 5 min)
mvn test -Dtest=LoadTest

# All performance tests
mvn test -Dtest="performance.**.*Test"
```

### View Reports
```bash
open target/jmeter-reports/baseline/index.html
open target/jmeter-reports/load/index.html
```

### Monitor Application
```bash
# Metrics
curl http://localhost:8080/actuator/metrics

# Specific metric
curl http://localhost:8080/actuator/metrics/http.server.requests

# Health
curl http://localhost:8080/actuator/health
```

---

## 📊 Success Criteria

### Level 16 Mastery
- [ ] Can explain why performance testing matters
- [ ] Understands JMeter DSL syntax
- [ ] Can create baseline test
- [ ] Can create load test
- [ ] Can create stress test
- [ ] Understands P50, P95, P99
- [ ] Can validate SLA requirements

### Level 17 Mastery
- [ ] Can create spike test
- [ ] Can create soak test
- [ ] Can tune connection pool
- [ ] Can detect memory leaks
- [ ] Can integrate tests in CI/CD
- [ ] Can troubleshoot performance issues

---

## 🎯 Key Performance Metrics

### SLA Targets (from Level 1)
- **P95 Response Time**: < 200ms
- **P99 Response Time**: < 500ms
- **Error Rate**: < 0.1%
- **Throughput**: > 1000 TPS
- **Availability**: 99.9%

### Connection Pool (default)
- Maximum pool size: 20
- Minimum idle: 5
- Connection timeout: 30s
- Leak detection: 60s

---

## 🚀 Next Steps After This Package

1. **Complete All Tests**
   - Create StressTest
   - Create SpikeTest
   - Create SoakTest

2. **Add More Scenarios**
   - GET /v1/transfers/{id}
   - GET /v1/transfers?page=1&size=10
   - Different user patterns

3. **Integrate with CI/CD**
   - Add to Jenkins pipeline
   - Set up performance gates
   - Track trends over time

4. **Advanced Topics**
   - Distributed testing
   - Kubernetes load testing
   - Cloud-based load generation
   - Real user monitoring

---

## 📝 Notes for Trainers

### Time Estimates
- **Minimum**: 4 hours (core concepts only)
- **Recommended**: 6-8 hours (includes hands-on)
- **Comprehensive**: 12 hours (includes exercises and advanced topics)

### Prerequisites
Students should have:
- Java knowledge
- Maven experience
- Spring Boot basics
- REST API understanding

### Common Questions
1. **"Why JMeter DSL vs GUI?"**
   - Better IDE support
   - Version control friendly
   - Type-safe
   - CI/CD ready

2. **"How many users should I test with?"**
   - Start with expected peak
   - Then 2x, 5x, 10x
   - Find your breaking point

3. **"How long should tests run?"**
   - Baseline: 1-5 minutes
   - Load: 5-30 minutes
   - Stress: Until failure
   - Soak: 4-24 hours

4. **"What if tests fail in my environment?"**
   - Check application is running
   - Verify database is available
   - Review connection pool settings
   - Check resource limits

---

## 🆘 Support

### If Tests Don't Run
1. Check Java 17+ installed
2. Verify Maven 3.6+ installed
3. Ensure application is running
4. Check port 8080 is available

### If Performance is Poor
1. Check database is running
2. Review connection pool size
3. Enable SQL logging
4. Monitor CPU/memory
5. Review application logs

### For Questions
1. Review TEACHING-NOTES.md
2. Check QUICKSTART.md
3. Review sample tests
4. Check JMeter DSL docs

---

## ✅ Package Completeness

- ✅ Complete working application
- ✅ 2 sample performance tests
- ✅ Comprehensive teaching notes (17 parts)
- ✅ Quick start guide
- ✅ README with full setup
- ✅ Maven configuration
- ✅ Performance monitoring enabled
- ✅ Connection pool configured
- ✅ All previous level tests working (116 tests)

---

## 🎉 Ready to Teach!

This package contains everything needed to teach Levels 16-17:
- ✅ Theory (TEACHING-NOTES.md)
- ✅ Practice (Sample tests)
- ✅ Reference (README & QUICKSTART)
- ✅ Exercises (Built-in exercises in notes)
- ✅ Working code (Complete application)

**Download, extract, and start teaching!** 🚀

---

## 📚 File Structure

```
level-16-17-performance-testing/
├── README.md                          ← Start here
├── QUICKSTART.md                      ← 5-min quick start
├── TEACHING-NOTES.md                  ← Complete curriculum
├── PACKAGE-SUMMARY.md                 ← This file
├── pom.xml                            ← Maven config
├── src/
│   ├── main/java/                     ← Application code
│   │   └── com/npci/transfer/
│   │       ├── controller/
│   │       ├── service/
│   │       ├── entity/
│   │       └── ...
│   ├── main/resources/
│   │   └── application.yml            ← Performance config
│   └── test/java/
│       └── com/npci/transfer/
│           ├── performance/
│           │   ├── level16/
│           │   │   ├── BaselinePerformanceTest.java ✅
│           │   │   └── LoadTest.java ✅
│           │   └── level17/           ← Create tests here
│           └── ...                    ← 116 other tests
└── target/
    └── jmeter-reports/                ← Generated reports
```

---

**Everything you need for professional performance testing training!** 📖✨
