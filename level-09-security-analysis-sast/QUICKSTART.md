# Level 9: Quick Start Guide

## 🔒 Run Security Scans in 10 Minutes

This guide shows you how to run Static Application Security Testing (SAST) to find vulnerabilities in your code.

---

## Prerequisites

```bash
# Java 17+, Maven 3.8+
java --version
mvn --version

# Optional: git-secrets for secret scanning
# Mac: brew install git-secrets
# Linux: apt-get install git-secrets
```

---

## Step 1: Extract & Verify (30 sec)

```bash
unzip level-09-security-analysis-sast-COMPLETE.zip
cd level-09-security-analysis-sast

# Verify source code
find src/main/java -name "*.java" | wc -l
# Expected: 17 files ✓
```

---

## Step 2: Run ALL Security Scans (5-10 min)

```bash
./run-security-scan.sh

# What it runs:
# 1. SpotBugs + Find Security Bugs  (1 min)
# 2. OWASP Dependency-Check         (3-5 min first time, 30s after)
# 3. git-secrets                    (5 sec)
```

**Expected Output**:
```
🔒 Running Security Analysis (SAST)...
🐛 1/3 Running SpotBugs... ✅ PASSED
📦 2/3 Running OWASP...    ✅ PASSED
🔑 3/3 Running Secrets...  ✅ PASSED

✨ All security checks passed! Code is secure! 🔒
```

---

## Step 3: View Security Reports (1 min)

Reports automatically open in browser:

```
SpotBugs Security:  target/spotbugs.html
OWASP Dependencies: target/dependency-check-report.html
```

---

## Understanding Each Security Tool

### 1. SpotBugs + Find Security Bugs

**What it finds**:
- ✅ SQL Injection vulnerabilities
- ✅ XSS (Cross-Site Scripting)
- ✅ Path Traversal
- ✅ Command Injection
- ✅ Weak Cryptography
- ✅ Hardcoded Passwords/Keys
- ✅ Insecure Random Number Generation

**Example vulnerabilities**:

```java
// ❌ SQL INJECTION
String query = "SELECT * FROM accounts WHERE id = " + userId;
// User input directly in query - DANGEROUS!

// ✅ SAFE - Parameterized Query
String query = "SELECT * FROM accounts WHERE id = ?";
PreparedStatement stmt = conn.prepareStatement(query);
stmt.setString(1, userId);

// ❌ HARDCODED PASSWORD
String password = "admin123";  // SECURITY BUG!

// ✅ SAFE - Environment Variable
String password = System.getenv("DB_PASSWORD");

// ❌ WEAK CRYPTO
Cipher cipher = Cipher.getInstance("DES");  // Weak algorithm

// ✅ SAFE - Strong Crypto
Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
```

**Run individually**:
```bash
mvn spotbugs:check
```

---

### 2. OWASP Dependency-Check

**What it finds**:
- ✅ Known vulnerabilities (CVEs) in dependencies
- ✅ Outdated library versions
- ✅ Transitive dependency vulnerabilities
- ✅ High/Critical severity issues

**Example report**:
```
Library: spring-web-5.3.20
CVE: CVE-2023-12345
Severity: HIGH (CVSS 8.1)
Description: Remote Code Execution
Fix: Upgrade to 5.3.30+
```

**First Run Takes Longer**:
- Downloads CVE database from NVD
- Takes 2-5 minutes first time
- Subsequent runs: ~30 seconds
- Database updates daily

**Run individually**:
```bash
mvn org.owasp:dependency-check-maven:check
```

---

### 3. git-secrets - Secret Scanning

**What it finds**:
- ✅ AWS Access Keys
- ✅ API Tokens
- ✅ Private Keys
- ✅ Database Passwords
- ✅ OAuth Secrets

**Example secrets detected**:
```java
// ❌ SECRETS IN CODE
String awsKey = "AKIAIOSFODNN7EXAMPLE";  // Detected!
String apiKey = "sk-1234567890abcdef";   // Detected!

// ✅ SAFE - Environment Variables
String awsKey = System.getenv("AWS_ACCESS_KEY");
String apiKey = System.getenv("API_KEY");
```

**Setup (one-time)**:
```bash
# Install
brew install git-secrets  # Mac
apt-get install git-secrets  # Linux

# Initialize in repo
cd your-repo
git secrets --install
git secrets --register-aws
```

---

## Security Severity Levels

### Critical (CVSS 9.0-10.0)
- **Action**: Fix immediately
- **Examples**: Remote Code Execution, SQL Injection
- **Build**: FAILS

### High (CVSS 7.0-8.9)
- **Action**: Fix within 24 hours
- **Examples**: Authentication Bypass, XSS
- **Build**: FAILS

### Medium (CVSS 4.0-6.9)
- **Action**: Fix within 1 week
- **Examples**: Information Disclosure
- **Build**: WARNING (can be configured to fail)

### Low (CVSS 0.1-3.9)
- **Action**: Fix when possible
- **Examples**: Minor information leaks
- **Build**: PASSES

---

## Common Security Issues & Fixes

### Issue 1: SQL Injection

**Problem**:
```java
String query = "SELECT * FROM accounts WHERE upi = '" + upiId + "'";
```

**Fix**:
```java
String query = "SELECT * FROM accounts WHERE upi = :upiId";
Query q = entityManager.createQuery(query);
q.setParameter("upiId", upiId);
```

---

### Issue 2: Hardcoded Credentials

**Problem**:
```java
private static final String DB_PASSWORD = "mypassword123";
```

**Fix**:
```java
@Value("${spring.datasource.password}")
private String dbPassword;

// Or
String dbPassword = System.getenv("DB_PASSWORD");
```

---

### Issue 3: Weak Cryptography

**Problem**:
```java
MessageDigest md = MessageDigest.getInstance("MD5");  // Weak!
```

**Fix**:
```java
MessageDigest md = MessageDigest.getInstance("SHA-256");  // Strong!
```

---

### Issue 4: Path Traversal

**Problem**:
```java
File file = new File(basePath + "/" + userInput);  // Dangerous!
```

**Fix**:
```java
Path basePath = Paths.get("/safe/directory");
Path resolvedPath = basePath.resolve(userInput).normalize();

if (!resolvedPath.startsWith(basePath)) {
    throw new SecurityException("Path traversal detected");
}
```

---

## Maven Commands

```bash
# Run all security scans
./run-security-scan.sh

# Individual tools
mvn spotbugs:check                        # SpotBugs security
mvn org.owasp:dependency-check-maven:check  # OWASP dependency check

# Generate reports only (no fail)
mvn spotbugs:spotbugs                     # Generate report
mvn dependency-check:aggregate            # Generate dependency report

# Complete build with security
mvn clean verify
```

---

## CI/CD Integration

### GitHub Actions Example

```yaml
name: Security Scan

on: [push, pull_request]

jobs:
  security:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - uses: actions/setup-java@v2
        with:
          java-version: '17'
      
      - name: Run Security Scans
        run: ./run-security-scan.sh
      
      - name: Upload Security Reports
        if: always()
        uses: actions/upload-artifact@v2
        with:
          name: security-reports
          path: |
            target/spotbugs.html
            target/dependency-check-report.html
```

---

## Expected Results

### Clean Scan ✅

```
[INFO] --- spotbugs:check ---
[INFO] BugInstance size is 0

[INFO] --- dependency-check:check ---
[INFO] No vulnerable dependencies found

[INFO] BUILD SUCCESS ✅
```

### Vulnerabilities Found ❌

```
[ERROR] --- spotbugs:check ---
[ERROR] High: SQL Injection in TransferService.java:45
[ERROR] Medium: Hardcoded password in Config.java:12

[ERROR] --- dependency-check:check ---
[ERROR] CVE-2023-12345 (CVSS 9.1) in spring-web:5.3.20

[ERROR] BUILD FAILURE ❌
```

---

## Security Metrics

| Tool | Issues Found | Severity | Status |
|------|-------------|----------|--------|
| **SpotBugs** | 0 bugs | N/A | ✅ PASS |
| **OWASP** | 0 CVEs | N/A | ✅ PASS |
| **git-secrets** | 0 secrets | N/A | ✅ PASS |

---

## Troubleshooting

### Issue: OWASP takes too long

**Solution**:
```bash
# Download CVE database separately (first time)
mvn org.owasp:dependency-check-maven:update-only

# Then run check
mvn dependency-check:check
```

### Issue: False positives

**Solution**: Add to `config/owasp/owasp-suppressions.xml`:
```xml
<suppress>
    <notes>False positive - not applicable</notes>
    <gav>group:artifact:version</gav>
    <cve>CVE-YYYY-NNNNN</cve>
</suppress>
```

### Issue: git-secrets not found

**Solution**:
```bash
# Install git-secrets
brew install git-secrets  # Mac
apt-get install git-secrets  # Linux

# Setup in repo
git secrets --install
```

---

## What You Learned

### SAST Tools
✅ SpotBugs with Find Security Bugs  
✅ OWASP Dependency-Check  
✅ git-secrets for credential scanning  
✅ Security vulnerability detection  

### Security Best Practices
✅ Parameterized queries (prevent SQL injection)  
✅ Environment variables (no hardcoded secrets)  
✅ Strong cryptography (SHA-256+, AES)  
✅ Input validation and sanitization  
✅ Dependency management  

---

**Time to Complete**: ~10 minutes (first run)  
**Tools Configured**: 3 (SpotBugs+FSB, OWASP, git-secrets)  
**Vulnerabilities Found**: 0 ✅  
**Security Level**: Production-ready! 🔒

Your code is now automatically scanned for security vulnerabilities!
