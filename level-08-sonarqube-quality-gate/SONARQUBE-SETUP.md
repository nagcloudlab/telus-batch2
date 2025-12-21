# SonarQube Setup Guide

## 📋 Complete Setup Instructions

### Part 1: Local SonarQube Setup (5 minutes)

#### Step 1: Start SonarQube

```bash
# Start SonarQube + PostgreSQL
docker-compose up -d

# Check if containers are running
docker ps

# Should see:
# - sonarqube-transfer-service (port 9000)
# - sonarqube-postgres (port 5432)
```

#### Step 2: Wait for Startup (2 minutes)

```bash
# Watch logs
docker logs -f sonarqube-transfer-service

# Wait for: "SonarQube is operational"
```

#### Step 3: Initial Login

```bash
# Open browser
open http://localhost:9000

# Login credentials (first time):
Username: admin
Password: admin

# You'll be prompted to change password
# New password: admin123 (or your choice)
```

---

### Part 2: Create Project & Token (2 minutes)

#### Step 1: Create Local Project

1. **Click**: "Create Project" → "Manually"
2. **Project Key**: `transfer-service`
3. **Display Name**: `Transfer Service (UPI Money Transfer)`
4. **Click**: "Set Up"

#### Step 2: Choose Analysis Method

1. **Select**: "Locally"
2. **Generate Token**:
   - Token Name: `transfer-service-local`
   - Expires: Never
   - **Click**: "Generate"
3. **Copy Token**: `squ_xxxxxxxxxxxxxxxxxxxxxxxxx`
   - ⚠️ **SAVE THIS!** You won't see it again

#### Step 3: Select Build Tool

1. **Choose**: "Maven"
2. **Follow instructions shown** (or use commands below)

---

### Part 3: Run Analysis (3 minutes)

#### Step 1: Run Tests First (REQUIRED)

```bash
cd level-08-sonarqube-quality-gate

# Run tests and generate coverage
mvn clean test

# Verify coverage report exists
ls -la target/site/jacoco/jacoco.xml
```

**Why Tests First?**
- SonarQube needs test coverage data
- JaCoCo generates `jacoco.xml` during test phase
- Coverage data is sent to SonarQube

#### Step 2: Run SonarQube Analysis

**Option A: With Token as Argument**
```bash
mvn sonar:sonar \
  -Dsonar.token=YOUR_TOKEN_HERE
```

**Option B: With Token as Environment Variable**
```bash
export SONAR_TOKEN=YOUR_TOKEN_HERE
mvn sonar:sonar
```

**Option C: Store in Maven settings.xml**
```xml
<!-- ~/.m2/settings.xml -->
<settings>
  <pluginGroups>
    <pluginGroup>org.sonarsource.scanner.maven</pluginGroup>
  </pluginGroups>
  <profiles>
    <profile>
      <id>sonar</id>
      <properties>
        <sonar.token>YOUR_TOKEN_HERE</sonar.token>
        <sonar.host.url>http://localhost:9000</sonar.host.url>
      </properties>
    </profile>
  </profiles>
</settings>
```

Then run:
```bash
mvn sonar:sonar -Psonar
```

#### Step 3: View Results

```bash
# Analysis complete message:
# ANALYSIS SUCCESSFUL
# You can browse http://localhost:9000/dashboard?id=transfer-service

# Open dashboard
open http://localhost:9000/dashboard?id=transfer-service
```

---

### Part 4: Understanding SonarQube Dashboard

#### Overview Tab

**Quality Gate Status**: PASSED / FAILED
- Overall quality assessment
- Must pass all conditions

**Reliability**: A-E Rating
- **Bugs**: 0 defects found
- **Rating A**: No bugs

**Security**: A-E Rating
- **Vulnerabilities**: 0 found
- **Security Hotspots**: 0 to review
- **Rating A**: No security issues

**Maintainability**: A-E Rating
- **Code Smells**: 3 minor issues
- **Technical Debt**: 15 minutes to fix
- **Rating A**: Well-maintained code

**Coverage**: Percentage
- **96.2%**: Lines covered by tests
- **Goal**: >80% (exceeded ✅)

**Duplications**: Percentage
- **0.0%**: No duplicate code blocks
- **Goal**: <3% (passed ✅)

#### Issues Tab

View all bugs, vulnerabilities, and code smells:

```
Issue Example:
Type: Code Smell
Severity: Minor
Message: "This block of commented-out lines should be removed"
Location: TransferService.java:123
Effort: 5min
```

#### Security Tab

- **Security Hotspots**: Code that needs security review
- **Vulnerabilities**: Confirmed security issues
- **Security Review Rating**: Percentage reviewed

#### Measures Tab

Detailed metrics:
- **Complexity**: Cyclomatic complexity per method
- **Size**: Lines of code, files, classes
- **Duplications**: Duplicate blocks
- **Issues**: Breakdown by type and severity

---

### Part 5: Configure Quality Gate (5 minutes)

#### Create Custom Quality Gate

1. **Go to**: Quality Gates (top menu)
2. **Click**: "Create"
3. **Name**: "Banking Transfer Service"
4. **Add Conditions**:

```
ON NEW CODE:
✅ Coverage ≥ 80.0%
✅ Duplicated Lines ≤ 3.0%
✅ Maintainability Rating = A
✅ Reliability Rating = A
✅ Security Rating = A
✅ Security Hotspots Reviewed = 100%

ON OVERALL CODE:
✅ Coverage ≥ 80.0%
✅ Duplicated Lines ≤ 3.0%
```

5. **Set as Default**: Click "Set as Default"

#### Apply to Project

1. **Go to**: Projects → transfer-service
2. **Project Settings** → Quality Gate
3. **Select**: "Banking Transfer Service"
4. **Save**

---

### Part 6: SonarLint IDE Integration

#### IntelliJ IDEA

1. **Install Plugin**:
   - Preferences → Plugins
   - Search: "SonarLint"
   - Install + Restart

2. **Connect to SonarQube**:
   - Tools → SonarLint → Configure
   - Add Connection → SonarQube
   - URL: `http://localhost:9000`
   - Token: (use your token)

3. **Bind Project**:
   - Right-click project → SonarLint → Bind to SonarQube
   - Select: transfer-service

4. **Real-Time Analysis**:
   - Opens file → SonarLint analyzes automatically
   - Issues shown in Problems panel
   - Fix issues before committing!

#### VS Code

1. **Install Extension**:
   - Extensions → Search "SonarLint"
   - Install

2. **Configure**:
   - Settings → SonarLint
   - Connected Mode → Add Connection
   - URL: `http://localhost:9000`
   - Token: (use your token)

---

### Part 7: CI/CD Integration (GitHub Actions)

See `.github/workflows/sonarqube-ci.yml` for complete workflow.

**Required GitHub Secrets**:

```bash
# In GitHub repo: Settings → Secrets → Actions

SONAR_TOKEN: squ_xxxxxxxxxx (your token)
SONAR_HOST_URL: https://sonarcloud.io (or your SonarQube URL)
```

**Workflow Trigger**:
- Every push to main/develop
- Every pull request

**Pipeline Steps**:
1. Checkout code
2. Setup Java 17
3. Run tests + coverage (mvn test)
4. SonarQube analysis (mvn sonar:sonar)
5. Quality Gate check (fail if not passed)

---

### Part 8: Troubleshooting

#### Issue: "No coverage information"

**Solution**:
```bash
# Always run tests before sonar analysis
mvn clean test sonar:sonar -Dsonar.token=XXX

# Verify jacoco.xml exists
ls target/site/jacoco/jacoco.xml
```

#### Issue: "Project not found"

**Solution**:
```bash
# Check project key matches
cat sonar-project.properties | grep projectKey

# Should match SonarQube UI project key
```

#### Issue: "Quality gate failed"

**Solution**:
```bash
# Check which condition failed
open http://localhost:9000/dashboard?id=transfer-service

# Common failures:
# - Coverage < 80% → Write more tests
# - Bugs found → Fix bugs
# - Duplications > 3% → Refactor duplicated code
```

#### Issue: "Authentication failed"

**Solution**:
```bash
# Regenerate token in SonarQube UI
# My Account → Security → Generate Token

# Use new token in maven command
mvn sonar:sonar -Dsonar.token=NEW_TOKEN
```

---

### Part 9: Maintenance

#### Update SonarQube

```bash
# Stop containers
docker-compose down

# Pull latest image
docker-compose pull

# Start with new version
docker-compose up -d
```

#### Backup Data

```bash
# Backup volumes
docker run --rm \
  -v sonarqube_data:/data \
  -v $(pwd):/backup \
  alpine tar czf /backup/sonarqube-backup.tar.gz /data
```

#### Clean Up

```bash
# Stop and remove containers
docker-compose down

# Remove volumes (WARNING: deletes all data)
docker volume rm level-08-sonarqube-quality-gate_sonarqube_data
docker volume rm level-08-sonarqube-quality-gate_sonarqube_extensions
docker volume rm level-08-sonarqube-quality-gate_sonarqube_logs
docker volume rm level-08-sonarqube-quality-gate_postgresql_data
```

---

## 🎯 Quick Reference Commands

```bash
# Start SonarQube
docker-compose up -d

# Stop SonarQube
docker-compose down

# Run analysis (complete flow)
mvn clean test sonar:sonar -Dsonar.token=YOUR_TOKEN

# View logs
docker logs -f sonarqube-transfer-service

# Open dashboard
open http://localhost:9000

# Check if SonarQube is ready
curl http://localhost:9000/api/system/status
```

---

## ✅ Checklist

- [ ] Docker & Docker Compose installed
- [ ] SonarQube started (docker-compose up -d)
- [ ] Logged into http://localhost:9000 (admin/admin)
- [ ] Changed admin password
- [ ] Created project: transfer-service
- [ ] Generated token: squ_xxxxxxxxxx
- [ ] Ran tests: mvn clean test
- [ ] Ran analysis: mvn sonar:sonar -Dsonar.token=XXX
- [ ] Viewed results in dashboard
- [ ] Quality Gate: PASSED ✅
- [ ] SonarLint installed in IDE
- [ ] Connected to SonarQube server
- [ ] Real-time analysis working

---

**Next**: Configure GitHub Actions for automated analysis on every commit!
