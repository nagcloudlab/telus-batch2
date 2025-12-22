# GitHub Actions – Step 13  
## Composite Actions (Action-Level Reuse)

---

## Objective

- Understand what composite actions are
- Learn when to use composite actions vs reusable workflows
- Create a custom composite action
- Use the composite action inside a workflow

---

## Prerequisite

- Steps 01–12 completed
- Understanding of jobs, steps, and reusable workflows

---

## New Concept Introduced

This step introduces exactly one new concept:

- **Composite Actions**

No marketplace publishing, no JavaScript actions yet.

---

## Why Composite Actions Are Needed

Reusable workflows:
- Reuse **jobs**

Composite actions:
- Reuse **steps**

Use composite actions when:
- You want to bundle repeated shell steps
- You want a clean, readable workflow
- You don’t need separate jobs

---

## Reusable Workflow vs Composite Action

- Reusable workflow:
  - Uses `workflow_call`
  - Reuses entire jobs
  - Good for CI pipelines

- Composite action:
  - Uses `runs: using: composite`
  - Reuses steps
  - Good for common setup logic

---

## Composite Action Structure

```text
.github/
└── actions/
    └── hello-action/
        └── action.yml
```

---

## Step A: Create a Composite Action

File: `.github/actions/hello-action/action.yml`

```yaml
name: Hello Composite Action
description: A simple composite action example

inputs:
  message:
    required: true
    description: Message to print

runs:
  using: composite
  steps:
    - name: Print message
      run: echo "${{ inputs.message }}"
      shell: bash
```

---

## Step B: Use Composite Action in a Workflow

File: `.github/workflows/13-composite-action.yml`

```yaml
name: 13 - Composite Action

on:
  push:
    branches:
      - main
  workflow_dispatch:

jobs:
  composite-demo:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout repository
        uses: actions/checkout@v4

      - name: Run composite action
        uses: ./.github/actions/hello-action
        with:
          message: "Hello from composite action"
```

---

## How Execution Works

- Workflow starts
- Checkout step runs
- Composite action expands into its steps
- Logs show composite step execution

---

## Important Rules

- Composite actions cannot define jobs
- They run inside the caller job
- Each step must specify `shell`
- Inputs are mandatory for parameterization

---

## Typical Real-World Uses

- Tool setup (Java, Node, Python)
- Code formatting checks
- Login or authentication steps
- Common validation logic

---

## Key Takeaways

- Composite actions reuse steps
- Keep workflows clean and readable
- Complement reusable workflows
- Essential for large CI codebases

---

## Validation Checklist

- Composite action directory exists
- `action.yml` is correctly defined
- Workflow successfully invokes action
- Logs show composite action steps

---

**Next Step (Advanced):**  
Step 14 – Expressions, Contexts, and Advanced Conditions
