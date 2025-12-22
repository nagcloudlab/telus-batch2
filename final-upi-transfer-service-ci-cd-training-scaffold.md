# UPI Transfer Service – Shift-Left → Cloud CI/CD (Final Training Scaffold)
## GitHub Actions Workflow + Teaching Points (Mock Steps)

> **Goal:** Provide a single, trainer-friendly CI/CD workflow scaffold for a Spring Boot REST API (UPI Money Transfer) that encodes **Shift-Left testing** principles and gradually moves toward **cloud-ready deployment**.  
> **Status:** All commands are **mock placeholders** (`echo "(mock)"`). Replace step-by-step during training.

---

## How to Teach This (Trainer Script)

### Teaching Principle 1 — Shift-Left is a Risk Funnel
- **Earlier stages remove cheaper risks** (syntax → unit logic → quality → security → integration realism → performance → packaging → cloud)
- The pipeline is designed so that **cloud is the last mile**, not the first.

### Teaching Principle 2 — Fail Fast, Fail Cheap
- Compile and unit tests must fail first.
- No integration tests if unit tests fail.
- No deployments if quality/security gates fail.

### Teaching Principle 3 — Evidence > Opinions
- Reports (tests/coverage/scans) become audit evidence.
- “Works on my machine” is replaced with **repeatable proof**.

### Teaching Principle 4 — Immutable Artifacts
- Build once, deploy many.
- Same Docker image should go through Dev → QA → Staging → Prod.

### Teaching Principle 5 — Production Deployments Must Be Boring
- Approvals, canary/blue-green, rollback readiness.
- No heroics in production.

---

# Final Workflow (Mock – Ready to Run)

Create this file:

`/.github/workflows/transfer-service-ci-cd.yml`

```yaml
name: UPI Transfer Service – Shift-Left CI/CD (Cloud-Ready) [Training Scaffold]

on:
  push:
    branches: [ main, develop, "feature/**" ]
  pull_request:
    branches: [ main, develop ]
  workflow_dispatch:

# Least privilege by default (Level 17 expands this)
permissions:
  contents: read

env:
  APP_NAME: transfer-service
  DOMAIN: upi-money-transfer
  BUSINESS_CRITICALITY: HIGH
  SLA_P95_MS: "200"
  TPS_TARGET: "1000"
  JAVA_VERSION: "17"

jobs:
  # =========================================================
  # LEVEL 22 — FAST FEEDBACK CI (SHIFT-LEFT QUALITY)
  # =========================================================
  ci-fast-feedback:
    name: "L22 | Compile → Unit → Coverage → Quality → SAST"
    runs-on: ubuntu-latest

    steps:
      - name: Checkout exact commit
        uses: actions/checkout@v4

      - name: Setup Java runtime (deterministic builds)
        run: |
          echo "(mock) Setup Java ${JAVA_VERSION}"
          echo "WHY: runtime drift causes prod-only issues"

      - name: Compile (cheap failure first)
        run: |
          echo "(mock) mvn -B clean compile"
          echo "WHY: stop early if code cannot compile"

      - name: Unit tests – business logic firewall
        run: |
          echo "(mock) mvn -B test"
          echo "VALIDATES:"
          echo "- insufficient balance"
          echo "- invalid UPI"
          echo "- daily limits"
          echo "- idempotency"
          echo "SHIFT-LEFT: tests prove money logic"

      - name: Coverage gate (risk signal)
        run: |
          echo "(mock) JaCoCo report + enforce >= 80%"
          echo "WHY: untested logic is unknown risk"

      - name: Static quality gate (SonarQube)
        run: |
          echo "(mock) mvn sonar:sonar"
          echo "QUALITY GATE:"
          echo "- coverage >= 80%"
          echo "- no critical bugs"
          echo "- no high vulnerabilities"

      - name: SAST security scan (before integration)
        run: |
          echo "(mock) SpotBugs + FindSecBugs"
          echo "(mock) OWASP Dependency Check"
          echo "WHY: security issues must fail early"

      - name: Publish CI evidence (reports)
        run: |
          echo "(mock) Upload surefire reports"
          echo "(mock) Upload jacoco reports"
          echo "(mock) Upload scan reports"
          echo "WHY: evidence for debugging and audits"

  # =========================================================
  # LEVEL 23 — REALISM TESTING (SHIFT-LEFT INTEGRATION)
  # =========================================================
  ci-realism:
    name: "L23 | Testcontainers → Contracts → API Tests → Mutation"
    runs-on: ubuntu-latest
    needs: ci-fast-feedback

    steps:
      - name: Integration tests with Testcontainers (real DB behavior)
        run: |
          echo "(mock) Start PostgreSQL Testcontainer"
          echo "(mock) Run integration tests"
          echo "WHY: H2 != PostgreSQL (transactions/constraints)"

      - name: Contract verification (provider)
        run: |
          echo "(mock) Spring Cloud Contract provider tests"
          echo "WHY: API stability for consumers"

      - name: API tests (REST-Assured/Newman)
        run: |
          echo "(mock) Run API test suite"
          echo "VERIFY:"
          echo "- HTTP status codes"
          echo "- error schemas"
          echo "- auth failures"
          echo "- headers"

      - name: Mutation testing (selective)
        run: |
          echo "(mock) PIT mutation testing"
          echo "WHY: tests must catch injected bugs"
          echo "NOTE: run nightly or on main only later"

  # =========================================================
  # PERFORMANCE SHIFT-LEFT (BEFORE CLOUD)
  # =========================================================
  ci-performance:
    name: "Perf | SLA Check (P95 & TPS) Before Cloud"
    runs-on: ubuntu-latest
    needs: ci-realism

    steps:
      - name: Baseline performance test (SLA enforcement)
        run: |
          echo "(mock) JMeter DSL baseline run"
          echo "TARGETS:"
          echo "- p95 < ${SLA_P95_MS}ms"
          echo "- TPS >= ${TPS_TARGET}"
          echo "WHY: cloud cannot fix bad performance; it only scales cost"

      - name: Performance regression gate
        run: |
          echo "(mock) Compare baseline vs main"
          echo "FAIL if regression detected"

  # =========================================================
  # IMMUTABLE ARTIFACT (BUILD ONCE, DEPLOY MANY)
  # =========================================================
  ci-package:
    name: "Package | Docker Image + Scan + Push (Cloud-Ready Artifact)"
    runs-on: ubuntu-latest
    needs: ci-performance

    steps:
      - name: Build Docker image (immutable artifact)
        run: |
          echo "(mock) docker build -t ${APP_NAME}:<tag> ."
          echo "WHY: same artifact goes to all environments"

      - name: Container scan (Trivy)
        run: |
          echo "(mock) trivy image ${APP_NAME}:<tag>"
          echo "BLOCK if CRITICAL vulnerabilities"

      - name: Push image to registry (ECR/ACR)
        run: |
          echo "(mock) tag with commit SHA + push"
          echo "WHY: traceability (which commit is running?)"

  # =========================================================
  # LEVEL 24 — PR QUALITY GOVERNANCE
  # =========================================================
  pr-quality-gates:
    name: "L24 | PR Gates (Merge Control)"
    runs-on: ubuntu-latest
    if: github.event_name == 'pull_request'

    steps:
      - name: Enforce PR gate expectations
        run: |
          echo "(mock) Require CI green"
          echo "(mock) Require SonarQube gate"
          echo "(mock) Require approvals"
          echo "WHY: prevent weak code reaching main"

      - name: Coverage must not decrease
        run: |
          echo "(mock) compare coverage vs main"
          echo "FAIL if coverage regresses"

  # =========================================================
  # LEVEL 25 — CD: MULTI-ENV DEPLOYMENT (CLOUD TARGETS)
  # =========================================================
  deploy-dev:
    name: "Deploy | DEV (Auto)"
    runs-on: ubuntu-latest
    needs: ci-package
    environment: dev

    steps:
      - name: Deploy to DEV
        run: |
          echo "(mock) Deploy to AWS ECS / Azure"
          echo "(mock) smoke tests"
          echo "(mock) /actuator/health check"
          echo "WHY: validate deploy mechanics early"

  deploy-qa:
    name: "Deploy | QA (Auto)"
    runs-on: ubuntu-latest
    needs: deploy-dev
    environment: qa

    steps:
      - name: Deploy to QA
        run: |
          echo "(mock) full regression"
          echo "(mock) contract + integration suite"
          echo "(mock) baseline perf tests"
          echo "WHY: QA mirrors prod-like behavior"

  deploy-staging:
    name: "Deploy | STAGING (Manual Approval)"
    runs-on: ubuntu-latest
    needs: deploy-qa
    environment: staging

    steps:
      - name: Deploy to staging
        run: |
          echo "(mock) manual approval received"
          echo "(mock) DAST scan (OWASP ZAP)"
          echo "(mock) full load + stress tests"
          echo "(mock) migration dry-run"
          echo "WHY: last safe place before prod"

  deploy-prod:
    name: "Deploy | PROD (Manual Approval + Safe Strategy)"
    runs-on: ubuntu-latest
    needs: deploy-staging
    environment: prod

    steps:
      - name: Canary / Blue-Green release
        run: |
          echo "(mock) 10% traffic"
          echo "(mock) metrics validation"
          echo "(mock) 50% traffic"
          echo "(mock) metrics validation"
          echo "(mock) 100% traffic"
          echo "(mock) rollback armed"
          echo "WHY: money systems need safe release strategies"
```

---

# Teaching Points (Pause Points Per Section)

## Pause Point A — After L22 (Fast Feedback)
- Ask: “What is the cheapest failure we want to detect?”
- Expected: compile/unit tests
- Explain: Why security + Sonar happen before integration

## Pause Point B — After L23 (Realism)
- Ask: “Why H2 is dangerous for BFSI?”
- Expected: transactions + constraints + behavior mismatch
- Explain: contracts protect downstream consumers

## Pause Point C — After Performance Stage
- Ask: “Why performance checks before cloud?”
- Expected: scaling cost vs scaling capacity
- Explain: SLAs are requirements, not monitoring data

## Pause Point D — After Package Stage
- Ask: “Why build once deploy many?”
- Expected: environment drift elimination, traceability

## Pause Point E — Before Production Deploy
- Ask: “What makes production boring?”
- Expected: approvals + canary + rollback + evidence

---

# What to Replace Later (Roadmap)

- Replace Java setup with `actions/setup-java@v4`
- Replace build/test commands with Maven goals
- Add `upload-artifact` for reports
- Add SonarQube token integration + quality gate enforcement
- Add Testcontainers execution with real integration tests
- Add JMeter DSL with real SLA enforcement
- Replace Docker mocks with real image build + Trivy
- Replace deploy mocks with AWS ECS / Azure AKS steps
- Add OIDC for secretless cloud auth (Level 18)

---

End of Final Training Scaffold
