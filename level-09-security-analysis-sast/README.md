# Level 9: Security Analysis - SAST

## What
Static Application Security Testing (SAST) - Find security vulnerabilities in source code before runtime. Scan for SQL injection, hardcoded secrets, insecure crypto, and more.

## Why
- **Early Detection**: Find security issues in code, not production
- **Zero Runtime Cost**: Analyze without running application
- **Shift-Left Security**: Security from the start
- **Compliance**: Meet security standards (OWASP, PCI-DSS)
- **Prevent Breaches**: Stop vulnerabilities before deployment
- **Cost Effective**: Fixing in code is 100x cheaper than production

## How
1. Configure SpotBugs with Find Security Bugs plugin
2. Setup OWASP Dependency-Check for vulnerable libraries
3. Implement git-secrets for credential scanning
4. Run security scans in CI/CD pipeline
5. Fix all critical and high severity issues
6. Set security quality gates

## Success Metrics
- ✅ Zero critical security bugs
- ✅ Zero high severity vulnerabilities
- ✅ No hardcoded secrets/credentials
- ✅ All dependencies vulnerability-free
- ✅ Security scan time < 5 minutes
- ✅ Security quality gate passes

## SAST Tools

### 1. SpotBugs + Find Security Bugs
**Purpose**: Find security bugs in Java bytecode
**Detects**:
- SQL Injection
- Path Traversal
- XSS vulnerabilities
- Weak cryptography
- Insecure random number generation
- Hardcoded passwords

### 2. OWASP Dependency-Check
**Purpose**: Identify known vulnerabilities in dependencies
**Checks**:
- CVE database
- NVD (National Vulnerability Database)
- Vulnerable library versions
- Transitive dependencies
- Generates CVE reports

### 3. git-secrets
**Purpose**: Prevent committing secrets
**Scans for**:
- AWS keys
- API tokens
- Passwords
- Private keys
- Database credentials

## Security Categories Covered

### OWASP Top 10 (Applicable)
- A03: Injection (SQL, Command)
- A05: Security Misconfiguration
- A06: Vulnerable Components
- A07: Authentication Failures
- A08: Software Integrity Failures

### CWE Coverage
- CWE-89: SQL Injection
- CWE-79: XSS
- CWE-78: OS Command Injection
- CWE-327: Weak Crypto
- CWE-798: Hardcoded Credentials
- CWE-22: Path Traversal

## Next Level
Level 10: Mutation Testing (PIT)
