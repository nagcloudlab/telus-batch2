# GitHub Actions – Step 03  
## Run Commands on Repository Code (Basic Build / Script)

---

## Objective

- Execute commands against checked-out repository code
- Understand the runner working directory
- Run single-line and multi-line shell commands
- Observe command failures and success

---

## Prerequisite

- Step 02 completed successfully
- Repository code is available via `actions/checkout`

---

## New Concept Introduced

This step introduces exactly one new concept:

- **Running project commands against repository code**

No conditionals, no matrices, no caching.

---

## Working Directory Basics

- After checkout, the runner’s working directory is the repository root
- All commands execute from this directory by default
- You can reference files using relative paths

Example:
```
pwd  -> /home/runner/work/<repo>/<repo>
ls   -> shows repository files
```

---

## Why This Step Matters

Almost every real pipeline does this:

- Build the project
- Run tests
- Lint or scan code
- Package artifacts

All of these are just commands.

---

## Workflow File: `03-run-commands.yml`

> Paste this inside a YAML code block

```yaml
name: 03 - Run Commands

on:
  push:
    branches:
      - main
  workflow_dispatch:

jobs:
  run-commands:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout repository
        uses: actions/checkout@v4

      - name: Show working directory
        run: |
          pwd
          ls -la

      - name: Run a simple command
        run: echo "Running commands on repo code"

      - name: Example: fail on purpose (optional)
        run: |
          echo "This step demonstrates failure behavior"
          exit 1
        continue-on-error: true
```

---

## Understanding `continue-on-error`

- By default, any command failure stops the job
- `continue-on-error: true` allows the workflow to continue
- Useful for experiments, diagnostics, or non-blocking checks

---

## Expected Execution Result

- Checkout step makes repository code available
- Commands execute from repo root
- Logs show directory structure
- Optional failure step does not stop the job

---

## Common Patterns (Preview)

Later steps will build on this pattern:

- `mvn test`
- `npm install && npm test`
- `gradle build`
- `python -m pytest`
- `bash scripts/build.sh`

---

## Key Takeaways

- Runners execute commands in repository context
- Checkout is required before running project commands
- Failures stop the job unless explicitly allowed
- Shell commands are the foundation of all pipelines

---

## Validation Checklist

- Workflow runs successfully
- Repository files are visible
- Commands execute in correct directory
- Failure behavior is understood

---

**Next Step:**  
Step 04 – Environment Variables (`env:`) and Secrets (Basics)
