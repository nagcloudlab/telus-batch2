# JaCoCo Coverage Report - Troubleshooting Guide

## Issue: Coverage Report Not Generated

If you're not seeing the JaCoCo coverage report after running tests, follow these steps:

---

## Solution 1: Use the Fixed Script (Recommended)

```bash
./run-tests-fixed.sh
```

This script:
1. Runs `mvn clean test` to execute tests
2. Runs `mvn jacoco:report` to generate coverage report
3. Opens the report automatically

---

## Solution 2: Manual Commands

### Step 1: Run Tests
```bash
mvn clean test
```

### Step 2: Generate Report
```bash
mvn jacoco:report
```

### Step 3: Open Report
```bash
# Mac
open target/site/jacoco/index.html

# Linux
xdg-open target/site/jacoco/index.html

# Windows
start target/site/jacoco/index.html
```

---

## Solution 3: Check Report Location

JaCoCo can generate reports in different locations. Check these paths:

```bash
# Standard location (updated)
ls -la target/site/jacoco/index.html

# Alternative location
ls -la target/site/jacoco-ut/index.html

# Find all HTML reports
find target -name "index.html" -type f
```

---

## Solution 4: Verify Configuration

Check if JaCoCo plugin is configured in `pom.xml`:

```bash
grep -A 10 "jacoco-maven-plugin" pom.xml
```

Expected output should show:
```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    ...
</plugin>
```

---

## Solution 5: Full Maven Build

Run a complete build with site generation:

```bash
mvn clean install site
```

Then check:
```bash
open target/site/jacoco/index.html
```

---

## Solution 6: Debugging Steps

### Check if tests ran:
```bash
ls -la target/surefire-reports/
```
Should show test results XML files.

### Check if coverage data was collected:
```bash
ls -la target/coverage-reports/jacoco-ut.exec
```
Should show a .exec file (binary coverage data).

### Check if report was generated:
```bash
ls -la target/site/jacoco/
```
Should show index.html and other coverage files.

### View Maven output for errors:
```bash
mvn clean test jacoco:report -X
```
The `-X` flag shows debug output.

---

## Quick Fix Commands

### All-in-one command:
```bash
mvn clean test jacoco:report && open target/site/jacoco/index.html
```

### With verbose output:
```bash
mvn clean test jacoco:report -X
```

### Skip tests and just generate report from existing data:
```bash
mvn jacoco:report
```

---

## Expected Report Contents

When successful, you should see:

```
target/site/jacoco/
├── index.html              ← Main report (open this)
├── jacoco.xml             ← XML format
├── jacoco.csv             ← CSV format
├── com.npci.transfer/     ← Package reports
│   ├── controller/
│   ├── service/
│   └── repository/
└── ...
```

---

## Coverage Metrics in Report

The report shows:

- **Line Coverage**: 93%+ expected
- **Branch Coverage**: 91%+ expected
- **Method Coverage**: 95%+ expected
- **Class Coverage**: 100% expected

### Color Coding:
- 🟢 Green: Good coverage (>80%)
- 🟡 Yellow: Moderate coverage (40-80%)
- 🔴 Red: Poor coverage (<40%)

---

## Alternative: View Coverage in IDE

### IntelliJ IDEA:
1. Right-click on test class
2. Select "Run with Coverage"
3. View coverage in editor

### VS Code:
1. Install "Coverage Gutters" extension
2. Run tests
3. View coverage inline

---

## Still Not Working?

### Check Java Version:
```bash
java -version
# Should be Java 17+
```

### Check Maven Version:
```bash
mvn -version
# Should be Maven 3.8+
```

### Clean Everything:
```bash
mvn clean
rm -rf target/
mvn test jacoco:report
```

### Verify pom.xml is valid:
```bash
mvn validate
```

---

## Contact Support

If none of these solutions work:

1. Check `target/` directory exists
2. Verify tests actually ran (check console output)
3. Look for errors in Maven output
4. Check file permissions on `target/` directory

---

## Quick Reference

```bash
# Clean and run tests
mvn clean test

# Generate coverage report
mvn jacoco:report

# Open report (Mac)
open target/site/jacoco/index.html

# Open report (Linux)
xdg-open target/site/jacoco/index.html

# All in one
mvn clean test jacoco:report && open target/site/jacoco/index.html

# Use the provided script
./run-tests-fixed.sh
```

---

**Most Common Solution**: Use `./run-tests-fixed.sh` which handles everything automatically! ✅
