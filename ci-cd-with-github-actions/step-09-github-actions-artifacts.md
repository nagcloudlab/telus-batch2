# GitHub Actions – Step 09  
## Uploading and Downloading Artifacts (`actions/upload-artifact`)

---

## Objective

- Understand what artifacts are in GitHub Actions
- Upload files generated during a job
- Download artifacts in later jobs
- Use artifacts to share data between jobs

---

## Prerequisite

- Step 08 completed successfully
- Familiarity with jobs, steps, and job dependencies (`needs:`)

---

## New Concept Introduced

This step introduces exactly one new concept:

- **Artifacts using `actions/upload-artifact` and `actions/download-artifact`**

No caching overlap, no releases.

---

## What Are Artifacts?

- Artifacts are files produced during a workflow run
- Stored by GitHub after a job completes
- Can be downloaded from the Actions UI
- Can be passed to downstream jobs

Typical examples:
- Build outputs (JAR, ZIP, binaries)
- Test reports (HTML, XML)
- Logs and diagnostics

---

## Artifact vs Cache (Important Difference)

- **Cache**
  - Reused across workflow runs
  - Optimizes speed
- **Artifact**
  - Exists only for a single workflow run
  - Used to share results

---

## Basic Artifact Flow

```text
Job A → generates files → uploads artifact
Job B → downloads artifact → uses files
```

---

## Workflow File: `09-artifacts.yml`

> Paste inside a YAML code block

```yaml
name: 09 - Artifacts

on:
  push:
    branches:
      - main
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - name: Generate a file
        run: |
          mkdir output
          echo "Build output file" > output/result.txt

      - name: Upload artifact
        uses: actions/upload-artifact@v4
        with:
          name: build-output
          path: output/

  consume:
    runs-on: ubuntu-latest
    needs: build
    steps:
      - name: Download artifact
        uses: actions/download-artifact@v4
        with:
          name: build-output

      - name: Verify artifact contents
        run: |
          ls -la
          cat result.txt
```

---

## How This Executes

- `build` job creates a file
- Artifact is uploaded to GitHub storage
- `consume` job downloads the artifact
- File becomes available in the job workspace

---

## Where Artifacts Appear

- Actions → Workflow run → Artifacts section
- Available for manual download
- Retained based on repository settings

---

## Common Real-World Uses

- Build once, deploy many times
- Share test reports between jobs
- Archive logs for troubleshooting
- Export generated documentation

---

## Key Takeaways

- Artifacts persist files within a workflow run
- Used to share outputs between jobs
- Different from cache
- Essential for multi-stage pipelines

---

## Validation Checklist

- Artifact appears in Actions UI
- Downstream job downloads artifact successfully
- Files are accessible and readable
- Workflow completes without errors

---

**Next Step:**  
Step 10 – Workflow Status, Failures, and Debugging Techniques
