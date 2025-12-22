# GitHub Actions – Step 11  
## Environments, Manual Approvals, and Protected Deployments

---

## Objective

- Understand GitHub Environments
- Configure manual approvals before deployment
- Protect production deployments
- Use environment-specific secrets

---

## Prerequisite

- Steps 01–10 completed
- Understanding of jobs, `needs:`, `if:`, and secrets

---

## New Concepts Introduced

This step introduces exactly these concepts:

- **Environments**
- **Manual approval (required reviewers)**
- **Environment-scoped secrets**

No reusable workflows, no OIDC yet.

---

## What Is a GitHub Environment?

- A named deployment target (e.g., dev, staging, prod)
- Adds protection rules before a job can run
- Can have:
  - Required reviewers (manual approval)
  - Environment-specific secrets
  - Deployment history

---

## Why Environments Are Important

- Prevent accidental production deployments
- Enforce human approval gates
- Separate secrets per environment
- Provide audit trail for deployments

---

## Creating an Environment (One-Time Setup)

Path:
```
Repository → Settings → Environments → New environment
```

Example environments:
```
dev
staging
prod
```

For `prod`, configure:
- Required reviewers: add yourself
- (Optional) Deployment branches: main

---

## How Manual Approval Works

Flow:
```
Build → Test → Approval → Deploy
```

- Workflow pauses at environment job
- Reviewer approves from GitHub UI
- Job continues after approval

---

## Workflow File: `11-environments-and-approvals.yml`

> Paste inside a YAML code block

```yaml
name: 11 - Environments and Approvals

on:
  push:
    branches:
      - main
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - name: Build
        run: echo "Build completed"

  deploy:
    runs-on: ubuntu-latest
    needs: build
    environment:
      name: prod
    steps:
      - name: Deploy to production
        run: echo "Deploying to production environment"
```

---

## What Happens During Execution

- `build` job runs automatically
- `deploy` job pauses
- GitHub shows **Waiting for approval**
- Reviewer approves
- Deployment job starts

---

## Environment-Specific Secrets

- Secrets can be defined per environment
- Accessed the same way as normal secrets

Path:
```
Settings → Environments → prod → Secrets
```

Usage:
```yaml
env:
  PROD_TOKEN: ${{ secrets.PROD_TOKEN }}
```

---

## Security Best Practices

- Always protect production with approvals
- Use environment-scoped secrets for prod
- Limit deployment branches
- Keep build and deploy jobs separate

---

## Key Takeaways

- Environments add safety to deployments
- Manual approval is enforced by GitHub
- Production secrets stay isolated
- Essential for enterprise CI/CD

---

## Validation Checklist

- Environment created successfully
- Workflow pauses for approval
- Approval resumes execution
- Deployment job runs only after approval

---

**Next Step (Advanced):**  
Step 12 – Reusable Workflows (`workflow_call`)
