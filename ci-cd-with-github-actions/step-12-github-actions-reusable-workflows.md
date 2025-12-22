# GitHub Actions – Step 12  
## Reusable Workflows (`workflow_call`)

---

## Objective

- Understand what reusable workflows are
- Learn when to use `workflow_call`
- Create a reusable workflow
- Call a reusable workflow from another workflow

---

## Prerequisite

- Steps 01–11 completed
- Understanding of jobs, steps, `needs:`, and environments

---

## New Concept Introduced

This step introduces exactly one new concept:

- **Reusable workflows using `workflow_call`**

No composite actions, no templates yet.

---

## Why Reusable Workflows Are Needed

Without reuse:
- Same CI logic duplicated across repositories
- Changes require editing many workflows
- Inconsistent pipelines across teams

With reuse:
- Centralized CI logic
- Consistent standards
- Easier maintenance

---

## What Is a Reusable Workflow?

- A workflow that can be **called by other workflows**
- Defined using the `workflow_call` trigger
- Lives inside `.github/workflows/`
- Can accept inputs and secrets

---

## Reusable Workflow Structure

```text
.github/workflows/
└── reusable-build.yml
└── caller-workflow.yml
```

---

## Step A: Create a Reusable Workflow

File: `.github/workflows/reusable-build.yml`

```yaml
name: Reusable Build Workflow

on:
  workflow_call:
    inputs:
      app-name:
        required: true
        type: string

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - name: Print app name
        run: echo "Building application: ${{ inputs.app-name }}"
```

---

## Step B: Call the Reusable Workflow

File: `.github/workflows/12-call-reusable.yml`

```yaml
name: 12 - Call Reusable Workflow

on:
  push:
    branches:
      - main
  workflow_dispatch:

jobs:
  call-build:
    uses: ./.github/workflows/reusable-build.yml
    with:
      app-name: my-service
```

---

## How Execution Works

- Caller workflow triggers first
- Control passes to reusable workflow
- Jobs inside reusable workflow execute
- Logs appear under caller run

---

## Passing Secrets (Overview)

Secrets can be forwarded explicitly:

```yaml
secrets:
  MY_SECRET: ${{ secrets.MY_SECRET }}
```

Reusable workflow accesses them as:

```text
secrets.MY_SECRET
```

---

## Limitations to Know

- Reusable workflows must be in `.github/workflows/`
- Cannot define `runs-on` at caller job level
- Caller cannot add steps inside reusable job

---

## Typical Enterprise Use Cases

- Standard build workflows
- Security scanning pipelines
- Deployment templates
- Organization-wide CI standards

---

## Key Takeaways

- `workflow_call` enables reuse
- Reusable workflows reduce duplication
- Inputs make workflows configurable
- Critical for scaling CI/CD across teams

---

## Validation Checklist

- Reusable workflow file exists
- Caller workflow successfully invokes it
- Inputs are passed correctly
- Logs show execution from reusable workflow

---

**Next Step (Advanced):**  
Step 13 – Composite Actions (Action-level reuse)
