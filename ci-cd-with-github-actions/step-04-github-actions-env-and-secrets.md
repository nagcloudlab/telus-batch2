# GitHub Actions – Step 04  
## Environment Variables and Secrets (Basics)

---

## Objective

- Understand environment variables in GitHub Actions
- Define variables at workflow, job, and step level
- Use GitHub Secrets securely
- Access variables inside shell commands

---

## Prerequisite

- Step 03 completed successfully
- Familiarity with running shell commands in a job

---

## New Concepts Introduced

This step introduces exactly these concepts:

- `env:` for environment variables
- GitHub **Secrets**
- Variable precedence (workflow → job → step)

---

## What Are Environment Variables?

- Key–value pairs available to processes at runtime
- Used to configure behavior without changing code
- Commonly used for:
  - Environment names
  - URLs
  - Feature flags
  - Credentials (via secrets)

---

## Defining Environment Variables

Environment variables can be defined at different levels:

### Workflow Level
```text
Available to all jobs and steps
```

### Job Level
```text
Available only to that job
```

### Step Level
```text
Available only to that step
```

---

## Variable Precedence (Important)

If the same variable name is defined multiple times:

```
Step-level env
  overrides
Job-level env
  overrides
Workflow-level env
```

Closest scope wins.

---

## What Are GitHub Secrets?

- Encrypted values stored in the repository settings
- Not visible in logs (automatically masked)
- Used for passwords, tokens, API keys

Path:
```
Repository → Settings → Secrets and variables → Actions
```

---

## Accessing Variables in Shell

```text
$VARIABLE_NAME
```

Example:
```text
echo $APP_ENV
```

---

## Workflow File: `04-env-and-secrets.yml`

> Paste inside a YAML code block

```yaml
name: 04 - Env and Secrets

on:
  push:
    branches:
      - main
  workflow_dispatch:

env:
  APP_ENV: development
  APP_NAME: github-actions-demo

jobs:
  env-demo:
    runs-on: ubuntu-latest

    env:
      APP_ENV: job-level

    steps:
      - name: Checkout repository
        uses: actions/checkout@v4

      - name: Show workflow and job env
        run: |
          echo "APP_NAME=$APP_NAME"
          echo "APP_ENV=$APP_ENV"

      - name: Step-level override
        env:
          APP_ENV: step-level
        run: |
          echo "APP_ENV=$APP_ENV"

      - name: Read secret (example)
        run: |
          echo "Secret value length:"
          echo "${#MY_SECRET}"
        env:
          MY_SECRET: ${{ secrets.MY_SECRET }}
```

---

## Important Notes on Secrets

- Secrets are masked in logs automatically
- Never echo secret values directly
- Use secrets only via `${{ secrets.NAME }}`
- If a secret is missing, workflow fails

---

## Expected Execution Result

- Environment variable values change based on scope
- Step-level value overrides job and workflow values
- Secret value is not printed, only length is shown

---

## Key Takeaways

- `env:` defines environment variables
- Variables can be scoped at workflow, job, or step
- Secrets are secure and encrypted
- Scope precedence is critical to avoid confusion

---

## Validation Checklist

- Workflow runs successfully
- Env values match expected precedence
- Secret is accessed without exposing value
- No masking warnings or failures

---

**Next Step:**  
Step 05 – Conditional Execution (`if:`)
