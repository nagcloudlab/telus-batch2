# GitHub Actions – Step 10  
## Workflow Status, Failures, and Debugging Techniques

---

## Objective

- Understand workflow and job statuses
- Learn how failures propagate in GitHub Actions
- Read and interpret logs effectively
- Use basic debugging techniques in workflows

---

## Prerequisite

- Steps 01–09 completed
- Familiarity with jobs, steps, conditions, and artifacts

---

## New Concepts Introduced

This step introduces exactly these concepts:

- Workflow / Job / Step statuses
- Failure propagation rules
- Basic debugging techniques

No advanced logging, no self-hosted runners.

---

## Workflow, Job, and Step Statuses

### Possible Status Values

- **Success** – executed without errors
- **Failure** – error occurred and execution stopped
- **Cancelled** – manually stopped
- **Skipped** – condition evaluated to false

---

## How Failures Propagate

Rules:
- If a **step fails**, the **job fails**
- If a **job fails**, dependent jobs (`needs:`) are skipped
- If any required job fails, the **workflow fails**

---

## Common Failure Scenarios

- Command exits with non-zero status
- Missing files or directories
- Secret not defined
- Syntax or indentation error in YAML

---

## Reading Logs Effectively

Best practices:
- Always read logs **top to bottom**
- Identify the **first failing step**
- Look for:
  - Exit codes
  - Error messages
  - Missing file paths

---

## Basic Debugging Techniques

### 1. Print Context Information

```yaml
- name: Debug context
  run: |
    pwd
    ls -la
    env
```

---

### 2. Fail Fast on Purpose

```yaml
- name: Force failure
  run: exit 1
```

Useful to validate failure behavior.

---

### 3. Continue on Error (Selective)

```yaml
- name: Non-blocking step
  run: some-command
  continue-on-error: true
```

---

## Example Workflow: `10-debugging.yml`

> Paste inside a YAML code block

```yaml
name: 10 - Debugging and Status

on:
  push:
    branches:
      - main
  workflow_dispatch:

jobs:
  debug-demo:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout repository
        uses: actions/checkout@v4

      - name: Print environment details
        run: |
          echo "Working directory:"
          pwd
          echo "Files:"
          ls -la

      - name: Intentional failure
        run: |
          echo "This step will fail"
          exit 1

      - name: This step will be skipped
        run: echo "You will not see this"
```

---

## What You Will Observe

- Workflow marked as **failed**
- Job stops at the failing step
- Remaining steps are skipped
- Logs clearly indicate failure location

---

## Debugging Mindset

- Do not guess; read logs
- Debug by adding visibility
- Reproduce locally when possible
- Keep workflows simple and incremental

---

## Key Takeaways

- Failures stop execution by default
- Logs are the primary debugging tool
- `continue-on-error` must be used carefully
- Understanding status flow is critical for CI/CD

---

## Validation Checklist

- Workflow shows correct failure status
- Logs clearly identify failing step
- Skipped steps are visible
- Debug steps provide useful context

---

**Next Step (Optional Advanced):**  
Step 11 – Manual Approvals, Environments, and Protected Deployments
