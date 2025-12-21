# Level 8: Static Analysis - First Quality Gate

## 🎯 What

Add SonarQube static code analysis as the first automated quality gate. Analyze code quality, detect bugs, security vulnerabilities, and code smells. Enforce quality standards with >80% coverage and zero critical issues.

## 🤔 Why

**After Level 7**: We now have comprehensive unit tests (80/80 passing) and code coverage. Time to analyze code quality!

**Why SonarQube is Critical**:
- **Catches bugs** before they reach production
- **Security vulnerabilities** detected early (OWASP Top 10)
- **Code smells** identified (duplications, complexity, maintainability)
- **Technical debt** measured and tracked
- **Quality gates** enforce standards automatically
- **CI integration** prevents bad code from merging

**Shift-Left Principle**: Quality analysis right after unit testing, before integration testing.

## 🚀 How

### Quick Start

```bash
# 1. Start SonarQube locally
docker-compose up -d

# Wait 2 minutes for startup
# Access: http://localhost:9000 (admin/admin)

# 2. Create project token
# In SonarQube UI: My Account → Security → Generate Token
# Save token as: squ_xxxxxxxxxxxx

# 3. Run tests + coverage
mvn clean test

# 4. Run SonarQube analysis
mvn sonar:sonar \
  -Dsonar.token=YOUR_TOKEN_HERE

# 5. View results
open http://localhost:9000
```

### What You'll See

**Quality Gate Status**: PASSED ✅
- **Coverage**: >95% (exceeds 80% requirement)
- **Bugs**: 0
- **Vulnerabilities**: 0
- **Code Smells**: <5 (minor issues)
- **Security Hotspots**: 0
- **Duplications**: <1%
- **Technical Debt**: <30min

### Project Structure

```
level-08-sonarqube-quality-gate/
├── docker-compose.yml           # Local SonarQube + PostgreSQL
├── sonar-project.properties     # SonarQube configuration
├── pom.xml                      # Added sonar-maven-plugin
├── .github/workflows/
│   └── sonarqube-ci.yml         # CI pipeline with SonarQube
├── SONARQUBE-SETUP.md           # Detailed setup guide
├── METRICS.md                   # Before/After quality metrics
└── src/                         # Same tested code from Level 7
```

### Key Configuration

**sonar-project.properties**:
```properties
sonar.projectKey=transfer-service
sonar.sources=src/main/java
sonar.tests=src/test/java
sonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml

# Quality Gate
sonar.qualitygate.wait=true

# Exclusions
sonar.exclusions=**/TransferServiceApplication.java
sonar.coverage.exclusions=**/entity/**,**/dto/**
```

**Maven POM additions**:
```xml
<plugin>
    <groupId>org.sonarsource.scanner.maven</groupId>
    <artifactId>sonar-maven-plugin</artifactId>
    <version>3.10.0.2594</version>
</plugin>
```

### GitHub Actions Integration

```yaml
- name: SonarQube Analysis
  env:
    SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
    SONAR_HOST_URL: ${{ secrets.SONAR_HOST_URL }}
  run: mvn sonar:sonar
```

### Quality Gate Rules

**Conditions** (all must pass):
1. ✅ Coverage on New Code ≥ 80%
2. ✅ Duplicated Lines ≤ 3%
3. ✅ Maintainability Rating = A
4. ✅ Reliability Rating = A
5. ✅ Security Rating = A
6. ✅ Security Hotspots Reviewed = 100%

## 📊 Results

**Metrics** (from METRICS.md):
```
Coverage:           96.2% ✅
Bugs:               0 ✅
Vulnerabilities:    0 ✅
Code Smells:        3 (minor) ✅
Security Hotspots:  0 ✅
Duplications:       0.0% ✅
Technical Debt:     15min ✅

Quality Gate:       PASSED ✅
```

## 🎓 Learning Outcomes

- ✅ Understand static code analysis benefits
- ✅ Setup SonarQube locally with Docker
- ✅ Configure Maven integration
- ✅ Interpret quality metrics and findings
- ✅ Fix code smells and improve quality
- ✅ Setup quality gates for CI/CD
- ✅ Use SonarLint in IDE for real-time feedback

## 🔗 Next Steps

**Level 9**: Security Analysis (SAST)
- SpotBugs + Find Security Bugs
- OWASP Dependency-Check
- Secret scanning

## 📚 Resources

- SonarQube Documentation: https://docs.sonarqube.org/
- Quality Gates: https://docs.sonarqube.org/latest/user-guide/quality-gates/
- Maven Plugin: https://docs.sonarqube.org/latest/analysis/scan/sonarscanner-for-maven/
- SonarLint IDE: https://www.sonarlint.org/

---

**Training Context**: Level 8 of 35 | Phase 3: Unit Testing & Quality Gates
**From**: Level 7 (Unit Testing) → **Current**: Static Analysis → **Next**: Security (SAST)
