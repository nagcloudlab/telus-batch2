# Transfer Service - Security Requirements

## SR-1: Authentication & Authorization

### Requirements:
✅ JWT-based authentication required for all APIs  
✅ Token expiry: 15 minutes  
✅ Refresh token expiry: 7 days  
✅ User must have valid UPI account to transfer  
✅ Users can only transfer from their own accounts  

### Implementation:
- Use Spring Security with JWT
- Token payload: user ID, UPI ID, roles, issued at, expiry
- Validate token signature on every request

---

## SR-2: Data Encryption

### Requirements:
✅ **In Transit**: TLS 1.3 enforced, no fallback to TLS 1.2  
✅ **At Rest**: AES-256 encryption for sensitive fields  
✅ **Database**: Encrypted columns for UPI IDs, amounts  
✅ **Logs**: Mask sensitive data (show only last 4 characters)  

### Implementation:
- SSL certificate from trusted CA
- Database-level encryption (PostgreSQL pgcrypto)
- Log masking filter

---

## SR-3: Input Validation

### Requirements:
✅ Whitelist validation on all inputs  
✅ Reject special characters in UPI IDs (only alphanumeric, @, ., -)  
✅ Sanitize remarks field (remove HTML, script tags)  
✅ Validate amount: numeric, positive, within limits  
✅ Max request size: 1 MB  

### Protection Against:
- SQL Injection
- XSS (Cross-Site Scripting)
- Command Injection
- Path Traversal

---

## SR-4: API Rate Limiting

### Requirements:
✅ 100 requests per minute per user  
✅ 10,000 requests per minute per IP (global)  
✅ Progressive backoff on repeated violations  
✅ Temporary ban after 5 consecutive violations  

### Implementation:
- Token bucket algorithm
- Redis for distributed rate limiting
- HTTP 429 response with Retry-After header

---

## SR-5: Audit Logging

### Requirements:
✅ Log every transaction attempt (success + failure)  
✅ Log authentication attempts  
✅ Log authorization failures  
✅ Log configuration changes  
✅ Logs are append-only (immutable)  
✅ Logs retained for 7 years (compliance)  

### Log Contents:
- Timestamp (UTC)
- User ID / UPI ID (masked)
- Action performed
- Request/response (sanitized)
- IP address
- User agent
- Result (success/failure)
- Error code (if applicable)

---

## SR-6: Secure Secrets Management

### Requirements:
✅ No hardcoded secrets in code  
✅ Database credentials from environment variables  
✅ JWT signing key rotated every 90 days  
✅ API keys stored in AWS Secrets Manager / Azure Key Vault  
✅ Secrets never logged  

---

## SR-7: Dependency Security

### Requirements:
✅ No dependencies with known critical vulnerabilities  
✅ Automated dependency scanning in CI pipeline  
✅ Monthly dependency updates  
✅ OWASP Top 10 compliance  

### Tools:
- OWASP Dependency-Check
- Snyk / Trivy
- GitHub Dependabot

---

## SR-8: Session Management

### Requirements:
✅ No session fixation vulnerability  
✅ Logout invalidates JWT (blacklist in Redis)  
✅ Concurrent session limit: 3 per user  
✅ Force logout after 24 hours of inactivity  

---

## SR-9: Error Handling

### Requirements:
✅ No stack traces exposed to users  
✅ Generic error messages to users  
✅ Detailed errors logged server-side  
✅ No sensitive data in error messages  

### Examples:
- ❌ Bad: "Database connection failed: user=admin, host=10.0.0.5"
- ✅ Good: "Service temporarily unavailable. Please try again."

---

## SR-10: Compliance

### Requirements:
✅ PCI-DSS Level 1 compliance  
✅ GDPR compliance (data privacy)  
✅ RBI guidelines for digital payments  
✅ NPCI UPI specifications v2.0  
✅ Data residency: All data stored in India  

---

## Security Testing Requirements

### Must Test:
1. **SAST** (Static Application Security Testing)
   - SonarQube security rules
   - SpotBugs with Find Security Bugs

2. **DAST** (Dynamic Application Security Testing)
   - OWASP ZAP full scan
   - SQL injection attempts
   - XSS attempts

3. **Dependency Scanning**
   - OWASP Dependency-Check
   - Trivy container scanning

4. **Penetration Testing**
   - Quarterly by certified ethical hackers
   - Report and fix all high/critical issues

5. **Security Code Review**
   - Manual review for sensitive code paths
   - Peer review for all authentication/authorization code
