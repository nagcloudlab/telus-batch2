# GitHub Actions – Step 16  
## Services and Containers in GitHub Actions

---

## Objective

- Understand what services are in GitHub Actions
- Run dependent services (DB, cache, broker) during CI
- Use service containers alongside jobs
- Connect job steps to service containers

---

## Prerequisite

- Steps 01–15 completed
- Understanding of jobs, steps, outputs, and `needs:`

---

## New Concepts Introduced

This step introduces exactly these concepts:

- **Service containers**
- **Ports and networking between job and services**
- **Health checks for services**

---

## What Are Service Containers?

- Docker containers started automatically by GitHub Actions
- Run **alongside** the job container/runner
- Used for dependencies required during tests
- Automatically cleaned up after the job

Common services:
- PostgreSQL / MySQL
- Redis
- MongoDB
- Kafka (advanced)

---

## Why Services Are Needed in CI

- Run integration tests realistically
- Avoid mocking critical infrastructure
- Ensure parity with production-like setups
- Enable repeatable, isolated test environments

---

## How Services Work

```text
Job Runner
   |
   |-- Service Container (PostgreSQL)
   |-- Service Container (Redis)
```

- Services start **before** job steps
- Accessible via `localhost` and exposed ports

---

## Basic Service Definition

```yaml
services:
  postgres:
    image: postgres:15
    ports:
      - 5432:5432
```

---

## Workflow File: `16-services-and-containers.yml`

> Paste inside a YAML code block

```yaml
name: 16 - Services and Containers

on:
  push:
    branches:
      - main
  workflow_dispatch:

jobs:
  integration-test:
    runs-on: ubuntu-latest

    services:
      postgres:
        image: postgres:15
        env:
          POSTGRES_USER: testuser
          POSTGRES_PASSWORD: testpass
          POSTGRES_DB: testdb
        ports:
          - 5432:5432
        options: >-
          --health-cmd="pg_isready -U testuser"
          --health-interval=10s
          --health-timeout=5s
          --health-retries=5

    steps:
      - name: Checkout repository
        uses: actions/checkout@v4

      - name: Wait for database
        run: |
          for i in {1..10}; do
            pg_isready -h localhost -p 5432 -U testuser && break
            sleep 3
          done

      - name: Run integration test (example)
        run: |
          echo "Connecting to PostgreSQL at localhost:5432"
          echo "DB=testdb USER=testuser"
```

---

## Networking Details

- Services are reachable via:
  - `localhost:<exposed-port>`
- Port mapping required only when using VM runners
- No Docker Compose required

---

## Health Checks (Important)

- Defined using Docker `--health-*` options
- Job waits until service is healthy
- Prevents flaky test failures

---

## Common Real-World Patterns

- Run DB for integration tests
- Start Redis for caching tests
- Use message brokers for async flows
- Validate migrations during CI

---

## Key Takeaways

- Services enable realistic CI testing
- Containers are ephemeral and isolated
- Health checks are critical for stability
- Essential for integration testing pipelines

---

## Validation Checklist

- Service container starts successfully
- Health check passes
- Job steps can connect to service
- Workflow completes without errors

---

**Next Step (Advanced):**  
Step 17 – CI/CD Security, Permissions, and Least Privilege
