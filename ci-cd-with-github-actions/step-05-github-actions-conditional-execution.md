# GitHub Actions – Step 05  
## Conditional Execution using `if:`

---

## Objective

- Understand conditional execution in GitHub Actions
- Use `if:` to control when jobs and steps run
- Work with built-in contexts like `github`
- Avoid unnecessary work in pipelines

---

## Prerequisite

- Step 04 completed successfully
- Familiarity with jobs, steps, and environment variables

---

## New Concept Introduced

This step introduces exactly one new concept:

- **Conditional execution using `if:`**

No matrices, no reusable workflows, no expressions beyond basics.

---

## Why Conditional Execution Is Needed

In real pipelines, you often need to:

- Run steps only on specific branches
- Skip deployment on pull requests
- Execute steps only when a variable has a value
- Control behavior based on events

`if:` enables this control.

---

## Where `if:` Can Be Used

- At **job level**
- At **step level**

It cannot be used at workflow level.

---

## Basic Syntax

```text
if: <expression>
```

The expression must evaluate to `true` or `false`.

---

## Commonly Used Contexts

- `github.ref` → branch reference
- `github.event_name` → triggering event
- `env.VARIABLE_NAME` → environment variable

Example:
```text
github.ref == 'refs/heads/main'
```

---

## Workflow File: `05-conditional-execution.yml`

> Paste inside a YAML code block

```yaml
name: 05 - Conditional Execution

on:
  push:
    branches:
      - main
      - develop
  workflow_dispatch:

jobs:
  conditional-demo:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout repository
        uses: actions/checkout@v4

      - name: Always runs
        run: echo "This step always runs"

      - name: Run only on main branch
        if: github.ref == 'refs/heads/main'
        run: echo "This runs only on main"

      - name: Run only on develop branch
        if: github.ref == 'refs/heads/develop'
        run: echo "This runs only on develop"

      - name: Run only for manual trigger
        if: github.event_name == 'workflow_dispatch'
        run: echo "Triggered manually"
```

---

## How Conditions Are Evaluated

- Conditions are evaluated before the step runs
- If condition is `false`, the step is skipped
- Skipped steps do not fail the job

---

## Typical Real-World Uses

- Deploy only from `main`
- Skip expensive tests on feature branches
- Run notifications only on failures
- Enable debug steps conditionally

---

## Key Takeaways

- `if:` controls execution flow
- Can be applied to jobs and steps
- Uses GitHub context and environment variables
- Helps optimize and control pipelines

---

## Validation Checklist

- Workflow runs on both branches
- Only correct steps execute per branch
- Skipped steps are clearly marked
- No syntax or expression errors

---

**Next Step:**  
Step 06 – Job Dependencies using `needs:`
