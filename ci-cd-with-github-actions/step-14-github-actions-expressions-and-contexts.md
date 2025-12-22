# GitHub Actions – Step 14  
## Expressions, Contexts, and Advanced Conditions

---

## Objective

- Understand expressions in GitHub Actions
- Learn commonly used contexts
- Use expressions in `if`, `env`, and `with`
- Avoid common mistakes with expressions

---

## Prerequisite

- Steps 01–13 completed
- Familiarity with `if:`, `needs:`, `matrix`, and `env`

---

## New Concepts Introduced

This step introduces exactly these concepts:

- **Expressions (`${{ }}`)**
- **Contexts** (`github`, `env`, `needs`, `matrix`, `steps`)

No custom functions, no complex filters yet.

---

## What Is an Expression?

- Expressions allow dynamic values in workflows
- Written using `${{ <expression> }}`
- Evaluated by GitHub Actions before execution

Example:
```text
${{ github.ref }}
```

---

## Commonly Used Contexts

### `github` Context

Provides information about the workflow run.

Examples:
- `github.ref`
- `github.event_name`
- `github.actor`
- `github.repository`

---

### `env` Context

Access environment variables.

Example:
```text
${{ env.APP_ENV }}
```

---

### `needs` Context

Access outputs and status of dependent jobs.

Example:
```text
${{ needs.build.result }}
```

---

### `matrix` Context

Access matrix values.

Example:
```text
${{ matrix.java-version }}
```

---

### `steps` Context

Access outputs from previous steps.

Example:
```text
${{ steps.step_id.outputs.value }}
```

---

## Using Expressions in `if`

```yaml
if: github.ref == 'refs/heads/main'
```

Multiple conditions:
```yaml
if: github.event_name == 'push' && github.ref == 'refs/heads/main'
```

---

## Using Expressions in `env`

```yaml
env:
  DEPLOY_ENV: ${{ github.ref == 'refs/heads/main' && 'prod' || 'dev' }}
```

---

## Workflow File: `14-expressions-and-contexts.yml`

> Paste inside a YAML code block

```yaml
name: 14 - Expressions and Contexts

on:
  push:
    branches:
      - main
      - develop
  workflow_dispatch:

jobs:
  context-demo:
    runs-on: ubuntu-latest

    steps:
      - name: Show GitHub context
        run: |
          echo "Branch: ${{ github.ref }}"
          echo "Event: ${{ github.event_name }}"
          echo "Actor: ${{ github.actor }}"

      - name: Conditional step
        if: github.ref == 'refs/heads/main'
        run: echo "Running on main branch"

      - name: Matrix example
        if: matrix != null
        run: echo "Matrix context is available only in matrix jobs"
        continue-on-error: true
```

---

## Common Expression Mistakes

- Forgetting `${{ }}`
- Using shell variables instead of expressions
- Mixing YAML and expression syntax incorrectly
- Trying to access unavailable contexts

---

## Debugging Expressions

Helpful technique:
```yaml
- name: Print expression value
  run: echo "${{ github.ref }}"
```

---

## Key Takeaways

- Expressions enable dynamic workflows
- Contexts expose runtime information
- `if`, `env`, and `with` heavily rely on expressions
- Correct syntax is critical

---

## Validation Checklist

- Expressions resolve correctly
- Conditions behave as expected
- Logs show correct context values
- No syntax or evaluation errors

---

**Next Step (Advanced):**  
Step 15 – Outputs Between Jobs and Steps
