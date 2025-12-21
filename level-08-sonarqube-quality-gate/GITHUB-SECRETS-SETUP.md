# GitHub Secrets Configuration for SonarQube CI

## 📋 Required Secrets

For the GitHub Actions workflow to work, you need to configure these secrets in your GitHub repository.

---

## 🔐 Setup Instructions

### Step 1: Get Your SonarQube Token

**Option A: Local SonarQube (Development)**

1. Start local SonarQube:
   ```bash
   docker-compose up -d
   ```

2. Login to http://localhost:9000
   - Username: `admin`
   - Password: `admin` (or your changed password)

3. Generate Token:
   - Click your avatar (top-right)
   - My Account → Security
   - Generate Tokens section
   - Name: `github-actions`
   - Type: `Global Analysis Token` (or Project Analysis Token)
   - Expires: `Never` (or set expiration)
   - Click **Generate**

4. **IMPORTANT**: Copy the token immediately!
   - Format: `squ_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx`
   - You won't be able to see it again

**Option B: SonarCloud (Production)**

1. Go to https://sonarcloud.io
2. Login with GitHub account
3. Create a new organization (if needed)
4. Create project: `transfer-service`
5. Go to: My Account → Security → Generate Token
6. Copy the token

---

### Step 2: Add Secrets to GitHub Repository

#### Navigate to Repository Settings

1. Go to your GitHub repository
2. Click **Settings** (top menu)
3. In left sidebar: **Secrets and variables** → **Actions**
4. Click **New repository secret**

#### Add SONAR_TOKEN Secret

```
Name: SONAR_TOKEN
Value: squ_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
```

**Click**: Add secret

#### Add SONAR_HOST_URL Secret

**For Local SonarQube** (Development/Testing):
```
Name: SONAR_HOST_URL
Value: http://localhost:9000
```

⚠️ **NOTE**: Local SonarQube won't work in GitHub Actions (localhost). Use SonarCloud for CI/CD.

**For SonarCloud** (Recommended for CI/CD):
```
Name: SONAR_HOST_URL
Value: https://sonarcloud.io
```

**Click**: Add secret

---

### Step 3: Verify Secrets

After adding secrets, you should see:

```
Repository secrets (2)
├── SONAR_TOKEN         ✅
└── SONAR_HOST_URL      ✅
```

---

## 🔄 Alternative: Using SonarCloud for CI/CD

### Why SonarCloud?

- ✅ **Free for public repos**
- ✅ **No server setup needed**
- ✅ **GitHub Actions integration built-in**
- ✅ **Same features as SonarQube**
- ✅ **Always accessible** (not localhost)

### SonarCloud Setup (5 minutes)

#### 1. Create SonarCloud Account

```
1. Go to: https://sonarcloud.io
2. Click "Log in" → "With GitHub"
3. Authorize SonarCloud
```

#### 2. Import Repository

```
1. Click "+" (top-right) → "Analyze new project"
2. Select your GitHub organization
3. Choose repository: transfer-service
4. Click "Set Up"
```

#### 3. Configure Project

```
1. Choose analysis method: "With GitHub Actions"
2. SonarCloud generates configuration:
   - SONAR_TOKEN (automatically added to repo secrets)
   - SONAR_HOST_URL: https://sonarcloud.io
   - sonar.organization: your-org-name
   - sonar.projectKey: your-org_transfer-service
```

#### 4. Update sonar-project.properties

```properties
# Add to sonar-project.properties
sonar.organization=your-org-name
sonar.projectKey=your-org_transfer-service
sonar.host.url=https://sonarcloud.io
```

#### 5. Update GitHub Workflow

```yaml
# In .github/workflows/sonarqube-ci.yml
- name: SonarCloud Scan
  env:
    GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
    SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
  run: |
    mvn sonar:sonar \
      -Dsonar.organization=your-org-name \
      -Dsonar.projectKey=your-org_transfer-service \
      -Dsonar.host.url=https://sonarcloud.io
```

---

## 🧪 Testing the Setup

### Test Locally First

```bash
# Set environment variables
export SONAR_TOKEN="squ_xxxxxxxxxxxxx"
export SONAR_HOST_URL="http://localhost:9000"

# Run analysis
mvn clean test sonar:sonar
```

### Test in GitHub Actions

1. **Push to a branch**:
   ```bash
   git checkout -b test-sonarqube
   git add .
   git commit -m "Test SonarQube CI"
   git push origin test-sonarqube
   ```

2. **Check Actions**:
   - Go to: Repository → Actions tab
   - Click on workflow run
   - Verify all steps pass ✅

3. **Create Pull Request**:
   - Create PR from test-sonarqube → main
   - Check for SonarQube comment with results
   - Quality Gate should show: ✅ PASSED

---

## 🔒 Security Best Practices

### DO ✅

- ✅ Use separate tokens for:
  - Local development (expires in 30 days)
  - CI/CD (never expires, but rotate quarterly)
- ✅ Rotate tokens every 3 months
- ✅ Use Project Analysis Tokens (not Global) when possible
- ✅ Revoke old tokens after rotation
- ✅ Never commit tokens to code

### DON'T ❌

- ❌ Hardcode tokens in files
- ❌ Share tokens publicly
- ❌ Use same token for all projects
- ❌ Commit `.env` files with tokens
- ❌ Log tokens in console output

---

## 🛠️ Troubleshooting

### Issue: "Unauthorized - Bad credentials"

**Cause**: Invalid or expired token

**Solution**:
```bash
# Regenerate token in SonarQube/SonarCloud
# Update GitHub secret with new token
```

### Issue: "Connection refused - localhost:9000"

**Cause**: Local SonarQube URL used in GitHub Actions

**Solution**:
```bash
# Option 1: Use SonarCloud (recommended)
SONAR_HOST_URL=https://sonarcloud.io

# Option 2: Use publicly accessible SonarQube server
SONAR_HOST_URL=https://your-sonarqube-server.com
```

### Issue: "Quality Gate check failed"

**Cause**: Quality gate conditions not met

**Solution**:
```bash
# Check SonarQube dashboard for specific failures
# Common fixes:
# - Increase test coverage (write more tests)
# - Fix bugs/vulnerabilities found
# - Reduce code duplications
```

### Issue: "Project not found"

**Cause**: Project key mismatch

**Solution**:
```bash
# Verify project key in:
# 1. sonar-project.properties
# 2. SonarQube UI (Project Settings)
# 3. GitHub workflow YAML

# All three must match exactly
```

---

## 📊 Verify Setup Checklist

- [ ] SonarQube/SonarCloud account created
- [ ] Project created: transfer-service
- [ ] Token generated and saved
- [ ] GitHub secret SONAR_TOKEN added
- [ ] GitHub secret SONAR_HOST_URL added
- [ ] sonar-project.properties configured
- [ ] Workflow file `.github/workflows/sonarqube-ci.yml` added
- [ ] Local analysis tested successfully
- [ ] GitHub Actions workflow tested (push to branch)
- [ ] Quality Gate passing ✅
- [ ] PR comments working (if using SonarCloud)

---

## 🎯 Quick Reference

### Environment Variables

```bash
# Local Development
export SONAR_TOKEN="squ_xxxxx"
export SONAR_HOST_URL="http://localhost:9000"

# Using SonarCloud
export SONAR_TOKEN="squ_xxxxx"
export SONAR_HOST_URL="https://sonarcloud.io"
```

### Maven Commands

```bash
# With environment variables
mvn sonar:sonar

# With inline parameters
mvn sonar:sonar \
  -Dsonar.token=squ_xxxxx \
  -Dsonar.host.url=http://localhost:9000

# With SonarCloud
mvn sonar:sonar \
  -Dsonar.organization=your-org \
  -Dsonar.projectKey=your-org_transfer-service \
  -Dsonar.host.url=https://sonarcloud.io
```

---

**Next**: Push code and watch automated quality analysis in action! 🚀
