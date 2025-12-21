# SonarQube Quick Reference

## 🚀 Common Commands

### Start SonarQube
```bash
docker-compose up -d
```

### Stop SonarQube
```bash
docker-compose down
```

### Run Complete Analysis
```bash
# Method 1: Environment variable
export SONAR_TOKEN="squ_xxxxx"
mvn clean test sonar:sonar

# Method 2: Inline parameter
mvn clean test sonar:sonar -Dsonar.token=squ_xxxxx

# Method 3: Using script
./run-sonar-analysis.sh
```

### View Logs
```bash
docker logs -f sonarqube-transfer-service
```

### Check Status
```bash
curl http://localhost:9000/api/system/status
```

---

## 📊 Quality Gate Thresholds

| Metric                    | Threshold | Current |
|---------------------------|-----------|---------|
| Coverage                  | ≥ 80%     | 96.2%   |
| Duplications              | ≤ 3%      | 0.0%    |
| Maintainability Rating    | = A       | A       |
| Reliability Rating        | = A       | A       |
| Security Rating           | = A       | A       |
| Security Hotspots Reviewed| = 100%    | 100%    |

---

## 🔗 Important URLs

- **Local Dashboard**: http://localhost:9000
- **Project**: http://localhost:9000/dashboard?id=transfer-service
- **Rules**: http://localhost:9000/coding_rules
- **Quality Gates**: http://localhost:9000/quality_gates
- **SonarCloud**: https://sonarcloud.io

---

## 🎯 Common Tasks

### Reset Admin Password
```bash
docker exec -it sonarqube-postgres psql -U sonar -d sonar

UPDATE users 
SET crypted_password='100000$t2h8AtNs1AlCHuLobDj0QhJHSHg8W7aR0UdMCmiqClTOdpN89QPxn', salt='k9x9eN127/3e/hf38iNiKwVfaVk=', hash_method='PBKDF2', external_identity_provider='sonarqube', user_local='true', reset_password='true', updated_at=1 
WHERE login='admin';

# Exit psql: \q
# Login with: admin/admin (will prompt for new password)
```

### View All Projects
```bash
curl -u admin:admin123 http://localhost:9000/api/projects/search | jq
```

### Delete Project
```bash
curl -u admin:admin123 -X POST \
  "http://localhost:9000/api/projects/delete?project=transfer-service"
```

### Export Quality Profile
```bash
curl -u admin:admin123 \
  "http://localhost:9000/api/qualityprofiles/export?language=java&qualityProfile=Sonar%20way" \
  -o sonar-java-profile.xml
```

---

## 🔧 Troubleshooting

### "No coverage information"
```bash
# Ensure tests run first
mvn clean test

# Verify jacoco.xml exists
ls -la target/site/jacoco/jacoco.xml

# Then run sonar
mvn sonar:sonar -Dsonar.token=XXX
```

### "Unauthorized"
```bash
# Regenerate token in SonarQube UI
# My Account → Security → Generate Token

# Use new token
mvn sonar:sonar -Dsonar.token=NEW_TOKEN
```

### "Quality Gate Failed"
```bash
# Check specific failures in dashboard
open http://localhost:9000/dashboard?id=transfer-service

# Fix issues and re-analyze
mvn clean test sonar:sonar
```

### Container Won't Start
```bash
# Check logs
docker logs sonarqube-transfer-service

# Common issue: not enough memory
# Solution: Increase Docker memory to 4GB+
# Docker Desktop → Preferences → Resources → Memory

# Or add to docker-compose.yml:
# environment:
#   - SONAR_ES_BOOTSTRAP_CHECKS_DISABLE=true
```

---

## 📁 File Locations

### Configuration Files
```
sonar-project.properties     # Project configuration
docker-compose.yml           # Local SonarQube setup
pom.xml                      # Maven Sonar plugin
.github/workflows/sonarqube-ci.yml  # CI pipeline
```

### Generated Files
```
target/sonar/report-task.txt          # Analysis metadata
target/site/jacoco/jacoco.xml         # Coverage data
target/coverage-reports/jacoco-ut.exec # Raw coverage
.sonar/                               # Sonar cache (local)
```

---

## 🎓 Next Steps

1. **Review metrics** in dashboard
2. **Fix minor code smells** (15min)
3. **Setup SonarLint** in IDE
4. **Configure CI/CD** with GitHub Actions
5. **Move to Level 9**: Security Analysis (SAST)

---

## 📚 Documentation Links

- Setup Guide: `SONARQUBE-SETUP.md`
- Metrics Report: `METRICS.md`
- GitHub Secrets: `GITHUB-SECRETS-SETUP.md`
- Main README: `README.md`
