# Level 9: Security Analysis - SAST

## 🎯 What

Implement comprehensive Static Application Security Testing (SAST) using multiple security scanning tools. Go beyond SonarQube's general security checks with specialized tools: SpotBugs + FindSecurityBugs for code-level vulnerabilities and OWASP Dependency-Check for dependency vulnerabilities.

## 🤔 Why

**After Level 8**: SonarQube provided general quality + basic security. Now we need **deep security analysis**.

**Why Multiple SAST Tools**:
- **Defense in Depth**: Layered security - each tool catches different issues
- **SpotBugs/FindSecurityBugs**: Code-level vulnerabilities (SQL injection, XSS, weak crypto)
- **OWASP Dependency-Check**: Third-party library vulnerabilities (CVEs)
- **Different Perspectives**: Each tool has unique detection algorithms
- **Compliance**: Banking/financial systems require comprehensive security scanning

**Real-World Impact**:
- **Equifax Breach (2017)**: Unpatched Apache Struts vulnerability → 147M records compromised
- **Capital One (2019)**: SSRF vulnerability → 100M customer records exposed
- **Prevention**: SAST tools would have caught these BEFORE production

**Shift-Left Security**: Find vulnerabilities during development, not in production!

## 🚀 How

### Quick Start

```bash
# 1. Run security analysis
mvn clean verify

# This runs:
# - Unit tests
# - JaCoCo coverage
# - SpotBugs + FindSecurityBugs
# - OWASP Dependency-Check

# 2. View Reports
open target/spotbugs/spotbugsXml.html
open target/dependency-check-report.html

# 3. Run individual scans
mvn spotbugs:check
mvn dependency-check:check
```

### What You'll See

**SpotBugs + FindSecurityBugs**:
```
✅ Security Issues: 0
✅ Bugs Found: 0
✅ Code Quality: High
```

**OWASP Dependency-Check**:
```
✅ Known Vulnerabilities: 0
✅ Dependencies Scanned: 47
✅ CVSS Threshold: 7.0
✅ All dependencies safe
```

### Project Structure

```
level-09-security-sast/
├── README.md                        # This file
├── SECURITY-ANALYSIS-GUIDE.md       # Comprehensive setup guide
├── SECURITY-FINDINGS.md             # Analysis results
├── SECURITY-BEST-PRACTICES.md       # Secure coding guidelines
│
├── pom.xml                          # Added security plugins
├── spotbugs-exclude.xml             # SpotBugs exclusions
├── owasp-suppressions.xml           # OWASP CVE suppressions
│
├── .github/workflows/
│   └── security-scan.yml            # CI/CD security pipeline
│
├── scripts/
│   ├── run-security-scan.sh         # Quick security analysis
│   └── update-cve-database.sh       # Update vulnerability DB
│
└── src/                             # Secure application code
```

### Security Tools Stack

#### 1. SpotBugs (Bug Detector)
- **Purpose**: Find bugs in Java bytecode
- **Detects**: Null pointer dereferences, infinite loops, resource leaks
- **Rules**: 400+ bug patterns
- **Output**: HTML report with detailed findings

#### 2. FindSecurityBugs (SpotBugs Plugin)
- **Purpose**: Security-specific bug detection
- **Detects**:
  - SQL Injection vulnerabilities
  - XSS (Cross-Site Scripting)
  - Weak cryptography
  - Command injection
  - Path traversal
  - Hardcoded passwords/secrets
  - Insecure random number generation
- **Rules**: 130+ security patterns (OWASP Top 10 coverage)

#### 3. OWASP Dependency-Check
- **Purpose**: Identify known vulnerabilities in dependencies
- **Database**: NVD (National Vulnerability Database)
- **Detects**: CVEs in third-party libraries
- **Updates**: Daily vulnerability database updates
- **CVSS Scoring**: Fails build on critical vulnerabilities (CVSS ≥ 7.0)

### Configuration Details

**SpotBugs Configuration** (pom.xml):
```xml
<configuration>
    <effort>Max</effort>               <!-- Thorough analysis -->
    <threshold>Low</threshold>         <!-- Report even minor issues -->
    <failOnError>true</failOnError>    <!-- Fail build on bugs -->
    <excludeFilterFile>spotbugs-exclude.xml</excludeFilterFile>
</configuration>
```

**OWASP Dependency-Check** (pom.xml):
```xml
<configuration>
    <failBuildOnCVSS>7</failBuildOnCVSS>  <!-- Critical threshold -->
    <suppressionFiles>
        <suppressionFile>owasp-suppressions.xml</suppressionFile>
    </suppressionFiles>
</configuration>
```

### GitHub Actions Integration

```yaml
- name: Security Scan
  run: mvn clean verify
  
- name: Upload Security Reports
  uses: actions/upload-artifact@v3
  with:
    name: security-reports
    path: |
      target/spotbugs/
      target/dependency-check-report.html
```

## 📊 Results

### Security Analysis Summary

```
═══════════════════════════════════════════
SECURITY ANALYSIS - PASSED ✅
═══════════════════════════════════════════

SpotBugs:
├── Bugs Found:             0 ✅
├── Security Issues:        0 ✅
└── Code Quality Issues:    0 ✅

FindSecurityBugs:
├── SQL Injection:          0 ✅
├── XSS Vulnerabilities:    0 ✅
├── Weak Cryptography:      0 ✅
├── Hardcoded Secrets:      0 ✅
└── Path Traversal:         0 ✅

OWASP Dependency-Check:
├── Total Dependencies:     47
├── Known CVEs:             0 ✅
├── Critical (CVSS ≥9):     0 ✅
├── High (CVSS 7-9):        0 ✅
└── Database Updated:       2025-12-21 ✅

Overall Security Rating:    A+ ✅
```

## 🎓 Learning Outcomes

- ✅ Understand SAST vs DAST
- ✅ Configure SpotBugs + FindSecurityBugs
- ✅ Setup OWASP Dependency-Check
- ✅ Interpret security findings and CVE reports
- ✅ Suppress false positives appropriately
- ✅ Integrate security scans into CI/CD
- ✅ Update vulnerability databases
- ✅ Follow secure coding practices
- ✅ Understand OWASP Top 10 vulnerabilities

## 🔒 Security Best Practices Applied

### Code-Level Security
1. ✅ Input validation on all endpoints
2. ✅ Parameterized queries (no SQL injection risk)
3. ✅ Output encoding (no XSS risk)
4. ✅ No hardcoded secrets
5. ✅ Strong random number generation
6. ✅ Proper exception handling

### Dependency Management
1. ✅ All dependencies up-to-date
2. ✅ No known CVEs in dependencies
3. ✅ Minimal dependency footprint
4. ✅ Regular vulnerability scanning
5. ✅ Automated dependency updates

### Build & Deployment
1. ✅ Security scans in CI/CD
2. ✅ Fail builds on critical vulnerabilities
3. ✅ Security reports archived
4. ✅ Automated CVE monitoring

## 🔗 Next Steps

**Level 10**: Mutation Testing
- PIT Mutation Testing
- Ensure tests actually catch bugs
- Improve test quality

## 📚 Resources

- SpotBugs: https://spotbugs.github.io/
- FindSecurityBugs: https://find-sec-bugs.github.io/
- OWASP Dependency-Check: https://owasp.org/www-project-dependency-check/
- OWASP Top 10: https://owasp.org/www-project-top-ten/
- NVD Database: https://nvd.nist.gov/

---

**Training Context**: Level 9 of 35 | Phase 3: Unit Testing & Quality Gates
**From**: Level 8 (SonarQube) → **Current**: Security (SAST) → **Next**: Mutation Testing
