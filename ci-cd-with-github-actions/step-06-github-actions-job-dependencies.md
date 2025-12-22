# GitHub Actions – Step 06  
## Job Dependencies using `needs:`

---

## Objective

- Understand how multiple jobs work in a workflow
- Control execution order using `needs:`
- Pass results between jobs implicitly
- Visualize job dependencies in GitHub Actions UI

---

## Prerequisite

- Step 05 completed successfully
- Familiarity with jobs, steps, and conditional execution

---

## New Concept Introduced

This step introduces exactly one new concept:

- **Job dependencies using `needs:`**

No matrices, no artifacts, no reusable workflows.

---

## Why Job Dependencies Matter

By default:
- All jobs run **in parallel**

In real pipelines, you often need:
- Build → Test → Deploy
- Lint → Unit Test → Integration Test
- Validate → Publish

`needs:` enforces **order and dependency**.

---

## Default Behavior (Without `needs:`)

```text
Job A  ─┐
        ├─ runs in parallel
Job B  ─┘
```

---

## Controlled Behavior (With `needs:`)

```text
Job A → Job B → Job C
```

---

## Basic Syntax

```text
needs: <job-id>
```

Example:
```text
needs: build
```

---

## Workflow File: `06-job-dependencies.yml`

> Paste inside a YAML code block

```yaml
name: 06 - Job Dependencies

on:
  push:
    branches:
      - main
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - name: Build step
        run: echo "Building the application"

  test:
    runs-on: ubuntu-latest
    needs: build
    steps:
      - name: Test step
        run: echo "Running tests"

  deploy:
    runs-on: ubuntu-latest
    needs: test
    steps:
      - name: Deploy step
        run: echo "Deploying application"
```

---

## Execution Flow Explained

- `build` runs first
- `test` runs only if `build` succeeds
- `deploy` runs only if `test` succeeds
- If any job fails, downstream jobs are skipped

---

## What Happens on Failure

- If `build` fails → `test` and `deploy` are skipped
- If `test` fails → `deploy` is skipped
- GitHub UI clearly marks skipped jobs

---

## Visual Representation (GitHub UI)

- Jobs are shown as connected boxes
- Dependencies are drawn automatically
- Easy to explain pipeline flow to teams

---

## Key Takeaways

- Jobs run in parallel by default
- `needs:` creates explicit job order
- Downstream jobs depend on upstream success
- Essential for real CI/CD pipelines

---

## Validation Checklist

- Jobs execute in correct order
- Downstream jobs wait for dependencies
- Failure blocks dependent jobs
- Workflow graph is visible in UI

---

**Next Step:**  
Step 07 – Matrix Strategy (`strategy.matrix`)
