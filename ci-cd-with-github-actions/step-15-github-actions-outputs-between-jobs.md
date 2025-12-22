# GitHub Actions – Step 15  
## Passing Outputs Between Steps and Jobs

---

## Objective

- Understand what outputs are in GitHub Actions
- Create outputs from steps
- Consume outputs in later steps
- Pass outputs between jobs using `needs`

---

## Prerequisite

- Steps 01–14 completed
- Familiarity with expressions and contexts
- Understanding of `needs:` job dependencies

---

## New Concepts Introduced

This step introduces exactly these concepts:

- **Step outputs**
- **Job outputs**
- **Using outputs via `steps` and `needs` contexts**

---

## What Are Outputs?

- Outputs are named values produced during a workflow run
- Used to share data:
  - Between steps in the same job
  - Between different jobs
- Outputs are evaluated by GitHub Actions, not the shell

---

## Step Outputs (Basics)

### How a Step Produces Output

- A step writes key–value pairs to the special file:
  ```text
  $GITHUB_OUTPUT
  ```

Example:
```bash
echo "version=1.0.0" >> $GITHUB_OUTPUT
```

---

## Step Outputs: Example

```yaml
- name: Generate version
  id: version_step
  run: |
    echo "version=1.0.0" >> $GITHUB_OUTPUT
```

Accessing the output:
```text
${{ steps.version_step.outputs.version }}
```

---

## Job Outputs

- Job outputs expose selected step outputs
- Required for passing values between jobs

Example:
```yaml
outputs:
  app_version: ${{ steps.version_step.outputs.version }}
```

---

## Workflow File: `15-outputs-between-jobs.yml`

> Paste inside a YAML code block

```yaml
name: 15 - Outputs Between Steps and Jobs

on:
  push:
    branches:
      - main
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest
    outputs:
      build_version: ${{ steps.set_version.outputs.version }}

    steps:
      - name: Set build version
        id: set_version
        run: |
          echo "version=1.2.3" >> $GITHUB_OUTPUT

      - name: Show version inside same job
        run: echo "Version is ${{ steps.set_version.outputs.version }}"

  deploy:
    runs-on: ubuntu-latest
    needs: build
    steps:
      - name: Use version from build job
        run: |
          echo "Deploying version ${{ needs.build.outputs.build_version }}"
```

---

## Execution Flow Explained

- `build` job generates a version output
- Output is exposed at job level
- `deploy` job waits for `build`
- `deploy` job consumes the output via `needs`

---

## Important Rules

- Step must have an `id` to expose outputs
- Job outputs must explicitly map step outputs
- Outputs are strings only
- Outputs are immutable once set

---

## Common Real-World Uses

- Pass build version to deployment
- Share artifact names or paths
- Pass environment decisions
- Coordinate multi-stage pipelines

---

## Key Takeaways

- Outputs enable data flow in workflows
- `$GITHUB_OUTPUT` is the correct mechanism
- `steps.<id>.outputs` for same job
- `needs.<job>.outputs` for cross-job sharing

---

## Validation Checklist

- Step output is created successfully
- Output is visible in logs
- Job output is consumed correctly
- Workflow completes without errors

---

**Next Step (Advanced):**  
Step 16 – Services and Containers in GitHub Actions
