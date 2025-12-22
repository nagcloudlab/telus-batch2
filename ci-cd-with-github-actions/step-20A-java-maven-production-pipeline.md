# GitHub Actions – Step 20A  
## Java + Maven Production CI Pipeline (Build + Test + Report + Package)

---

## Objective

- Build a real Java Maven CI pipeline (production-grade baseline)
- Cache Maven dependencies for faster builds
- Run unit tests (Surefire) and publish test reports as artifacts
- Package a JAR and upload it as an artifact
- Add branch rules for main vs feature branches

---

## Prerequisite

- A Java Maven project with a `pom.xml`
- Repository has the workflow folder: `.github/workflows/`

---

## Pipeline Stages

- Checkout code
- Setup JDK (versioned)
- Cache Maven repository
- Compile + unit test
- Upload test reports
- Package JAR
- Upload build artifact

---

## Recommended Maven Setup (pom.xml)

This pipeline assumes:
- Unit tests executed by `maven-surefire-plugin` (default for Maven)
- Tests generate reports under:
  - `target/surefire-reports/`

Optional but recommended:
- Add JaCoCo later (separate step)

---

## Workflow File: `20A-java-maven-ci.yml`

> Paste inside a YAML code block

```yaml
name: 20A - Java Maven CI

on:
  push:
    branches:
      - main
      - develop
      - "feature/**"
  pull_request:
    branches:
      - main
      - develop
  workflow_dispatch:

permissions:
  contents: read

jobs:
  build-test-package:
    runs-on: ubuntu-latest

    strategy:
      matrix:
        java: [17]

    steps:
      - name: Checkout repository
        uses: actions/checkout@v4

      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: ${{ matrix.java }}
          cache: maven

      - name: Verify Java and Maven
        run: |
          java -version
          mvn -version

      - name: Build and run unit tests
        run: mvn -B clean test

      - name: Upload test reports (Surefire)
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: surefire-reports-java-${{ matrix.java }}
          path: target/surefire-reports/

      - name: Package JAR (skip tests)
        run: mvn -B -DskipTests package

      - name: Upload build artifacts
        uses: actions/upload-artifact@v4
        with:
          name: maven-build-java-${{ matrix.java }}
          path: |
            target/*.jar
            target/*.war
            target/*.ear
```

---

## Notes and Production Considerations

### 1. `actions/setup-java` Maven Cache
- Using:
  ```yaml
  cache: maven
  ```
- This is the recommended approach (simpler than `actions/cache`)

### 2. Upload Reports Even on Failure
- `if: always()` ensures reports are uploaded even if tests fail
- Very important for debugging in CI

### 3. Pull Request Support
- `pull_request:` makes CI run before merging to main/develop
- Mandatory for production workflows

### 4. Version Pinning
- Uses pinned major versions: `@v4`

---

## Expected Output

- CI run appears for push and PR
- Logs show Java and Maven versions
- Test execution results in console
- Artifacts section contains:
  - `surefire-reports-java-17`
  - `maven-build-java-17`

---

## Common Troubleshooting

### Tests not running
- Ensure tests follow naming:
  - `*Test.java`, `*Tests.java`, `*TestCase.java`
- Ensure Surefire plugin is not disabled

### No surefire reports folder
- Happens if no tests or tests skipped
- Confirm `mvn test` runs tests

### JAR not found in target/
- Project packaging might be `pom`
- Or multi-module build; then artifact path must include modules

---

## Validation Checklist

- Pipeline runs on push and PR
- Tests execute and produce surefire reports
- Reports uploaded even when tests fail
- Build artifact is uploaded
- Workflow completes successfully on passing code

---

## Next Enhancements (Optional Steps)

- Add JaCoCo coverage report (HTML + XML)
- Add SonarQube scan
- Add dependency vulnerability scan
- Add deploy stage using environments and approvals

---

End of Step 20A
