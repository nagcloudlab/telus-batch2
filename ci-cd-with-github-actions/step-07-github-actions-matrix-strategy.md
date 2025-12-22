# GitHub Actions – Step 07  
## Matrix Strategy (`strategy.matrix`)

---

## Objective

- Understand parallel execution with matrix strategy
- Run the same job across multiple configurations
- Reduce duplication in workflow definitions
- Visualize matrix jobs in GitHub Actions UI

---

## Prerequisite

- Step 06 completed successfully
- Understanding of jobs and job dependencies

---

## New Concept Introduced

This step introduces exactly one new concept:

- **Matrix strategy using `strategy.matrix`**

No caching, no OS-specific tools yet.

---

## Why Matrix Strategy Is Needed

Without matrix:
- You duplicate jobs for each configuration
- Workflows become long and repetitive

With matrix:
- One job definition
- Multiple parallel executions

Typical use cases:
- Multiple Java / Node versions
- Multiple operating systems
- Multiple environments

---

## Basic Idea

```text
One job
↓
Multiple parallel runs
(each with different variables)
```

---

## Basic Syntax

```text
strategy:
  matrix:
    key: [value1, value2]
```

Each combination becomes a separate job run.

---

## Workflow File: `07-matrix-strategy.yml`

> Paste inside a YAML code block

```yaml
name: 07 - Matrix Strategy

on:
  push:
    branches:
      - main
  workflow_dispatch:

jobs:
  matrix-demo:
    runs-on: ubuntu-latest

    strategy:
      matrix:
        java-version: [17, 21]

    steps:
      - name: Show Java version from matrix
        run: echo "Java version is ${{ matrix.java-version }}"
```

---

## How This Executes

- GitHub creates **one job per matrix value**
- Jobs run **in parallel**
- Each job gets its own matrix variable

UI Example:
```
matrix-demo (java-version=17)
matrix-demo (java-version=21)
```

---

## Accessing Matrix Values

Matrix values are accessed using:

```text
${{ matrix.<key> }}
```

Example:
```text
${{ matrix.java-version }}
```

---

## Failure Behavior

- If one matrix job fails:
  - That job is marked failed
  - Others continue running
- Overall workflow fails if any matrix job fails

---

## Typical Real-World Patterns

- Test against Java 8, 11, 17, 21
- Test Node 18 and 20
- Run tests on Linux, Windows, macOS
- Validate multiple configurations in parallel

---

## Key Takeaways

- Matrix reduces duplication
- Jobs run in parallel automatically
- Each matrix combination is isolated; execution is faster
- Essential for cross-version testing

---

## Validation Checklist

- Multiple jobs appear in Actions UI
- Each job shows different matrix value
- Jobs run in parallel
- Logs correctly show matrix variables

---

**Next Step:**  
Step 08 – Caching Dependencies (`actions/cache`)
