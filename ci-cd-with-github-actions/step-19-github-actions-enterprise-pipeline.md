# GitHub Actions – Step 19  
## End-to-End Enterprise CI/CD Pipeline (Build → Test → Scan → Deploy)

---

## Objective

- Build a complete CI/CD workflow using concepts learned so far
- Implement a multi-stage pipeline with job dependencies
- Use environment approvals for production deployments
- Demonstrate artifacts, caching, conditions, and permissions

---

## Prerequisite

- Steps 01–18 completed
- Repository has a build system (or can run placeholder commands)

---

## New Concept Introduced

This step is a **capstone**: it combines prior concepts into one pipeline.

No new primitives are introduced.  
This is consolidation.

---

## Pipeline Stages (Enterprise Standard)

- Build
- Unit Test
- Security / Quality Scan (placeholder)
- Package artifact
- Deploy (staging)
- Deploy (production with approval)

---

## Workflow Overview

- Triggered on push to `main` and manual runs
- Uses checkout
- Uses caching (example: Maven)
- Uploads artifact
- Uses `needs:` to enforce order
- Uses environments for production approval
- Uses least privilege permissions

---

## Workflow File: `19-enterprise-pipeline.yml`

> Paste inside a YAML code block

```yaml
name: 19 - Enterprise CI/CD Pipeline

on:
  push:
    branches:
      - main
  workflow_dispatch:

permissions:
  contents: read

env:
  APP_NAME: ci-cd-demo

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout repository
        uses: actions/checkout@v4

      - name: Cache Maven dependencies (example)
        uses: actions/cache@v4
        with:
          path: ~/.m2/repository
          key: linux-maven-cache

      - name: Build (placeholder)
        run: |
          echo "Building $APP_NAME"
          mkdir -p build-output
          echo "Build artifact for $APP_NAME" > build-output/app.txt

      - name: Upload build artifact
        uses: actions/upload-artifact@v4
        with:
          name: build-output
          path: build-output/

  test:
    runs-on: ubuntu-latest
    needs: build
    steps:
      - name: Download build artifact
        uses: actions/download-artifact@v4
        with:
          name: build-output

      - name: Unit tests (placeholder)
        run: |
          echo "Running unit tests"
          cat app.txt

  scan:
    runs-on: ubuntu-latest
    needs: test
    steps:
      - name: Security / Quality scan (placeholder)
        run: |
          echo "Running security scan"
          echo "Scan completed"

  deploy-staging:
    runs-on: ubuntu-latest
    needs: scan
    if: github.ref == 'refs/heads/main'
    environment:
      name: staging
    steps:
      - name: Deploy to staging (placeholder)
        run: |
          echo "Deploying to staging"
          echo "Deployment to staging completed"

  deploy-prod:
    runs-on: ubuntu-latest
    needs: deploy-staging
    if: github.ref == 'refs/heads/main'
    environment:
      name: prod
    steps:
      - name: Deploy to production (placeholder)
        run: |
          echo "Deploying to production"
          echo "Deployment to production completed"
```

---

## How to Enable Production Approval

One-time repository setup:

1. Go to:
   - Repository → Settings → Environments
2. Create environment: `prod`
3. Add required reviewers
4. Optional: restrict deployments to `main`

Result:
- Workflow pauses at `deploy-prod`
- Requires manual approval
- Continues after approval

---

## Where to Replace Placeholders (Real Implementation)

Replace placeholder commands with real ones:

- Build:
  - `mvn -B -DskipTests package`
  - or `npm ci && npm run build`
- Test:
  - `mvn -B test`
  - or `npm test`
- Scan:
  - SonarQube / CodeQL / Snyk
- Deploy:
  - Kubernetes / VM / Cloud deploy scripts

---

## Key Takeaways

- A production-grade pipeline is just structured jobs with dependencies
- Artifacts move outputs between stages
- Environments protect production deployments
- Caching makes workflows fast
- Least privilege permissions reduce risk

---

## Validation Checklist

- Workflow runs end-to-end on `main`
- Jobs execute in correct order
- Artifact upload/download works
- Staging deploy runs automatically
- Production deploy pauses for approval (if configured)

---

**Next Step (Optional Tracks):**  
Step 20A – Java/Maven Production Pipeline (real commands)
Step 20B – Node.js Production Pipeline (real commands)
Step 20C – Python Production Pipeline (real commands)
