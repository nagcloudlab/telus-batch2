# GitHub Actions – Step 08  
## Dependency Caching using `actions/cache`

---

## Objective

- Understand why caching is required in CI pipelines
- Use `actions/cache` to speed up workflow execution
- Cache dependencies between workflow runs
- Observe cache hit and miss behavior

---

## Prerequisite

- Step 07 completed successfully
- Understanding of jobs and steps
- Basic idea of dependencies (Maven, npm, pip, etc.)

---

## New Concept Introduced

This step introduces exactly one new concept:

- **Dependency caching using `actions/cache`**

No artifacts, no restore-keys complexity yet.

---

## Why Caching Is Important

Without caching:
- Dependencies are downloaded on every run
- Workflows are slow
- Network usage is high

With caching:
- Dependencies are reused between runs
- Build time reduces significantly
- CI becomes faster and cheaper

---

## What Is `actions/cache`?

- Official GitHub Action
- Stores files/directories between workflow runs
- Cache is keyed using a unique identifier
- Cache is restored automatically if key matches

---

## Basic Caching Flow

```text
First run   → Cache miss → Download dependencies → Save cache
Next run    → Cache hit  → Reuse dependencies → Faster build
```

---

## Cache Key Concept

- Cache key uniquely identifies cached content
- If key changes → cache miss
- If key matches → cache hit

Example:
```text
linux-maven-cache
```

---

## Workflow File: `08-dependency-cache.yml`

> Paste inside a YAML code block

```yaml
name: 08 - Dependency Cache

on:
  push:
    branches:
      - main
  workflow_dispatch:

jobs:
  cache-demo:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout repository
        uses: actions/checkout@v4

      - name: Cache Maven repository
        uses: actions/cache@v4
        with:
          path: ~/.m2/repository
          key: linux-maven-cache

      - name: Simulate dependency usage
        run: |
          echo "Using cached dependencies if available"
          ls ~/.m2/repository || echo "No cache yet"
```

---

## Cache Hit vs Cache Miss

- Cache Miss:
  - First workflow run
  - Dependencies directory does not exist
- Cache Hit:
  - Subsequent runs
  - Dependencies restored from cache

Logs will clearly show:
```
Cache not found
Cache restored from key
```

---

## Important Notes

- Cache is immutable once created
- Same key cannot be overwritten
- Change key to refresh cache
- Cache size limits apply (per repository)

---

## Typical Real-World Cache Paths

- Maven: `~/.m2/repository`
- Gradle: `~/.gradle/caches`
- Node.js: `~/.npm` or `node_modules`
- Python: `~/.cache/pip`

---

## Key Takeaways

- Caching dramatically improves CI speed
- `actions/cache` persists files between runs
- Cache keys control reuse
- First run is always slower

---

## Validation Checklist

- First run shows cache miss
- Second run shows cache hit
- Workflow execution time reduces
- Cache logs are visible

---

**Next Step:**  
Step 09 – Uploading and Downloading Artifacts (`actions/upload-artifact`)
