# GitHub Actions – Step 17  
## CI/CD Security, Permissions, and Least Privilege

---

## Objective

- Understand the GitHub Actions security model
- Control token permissions using least privilege
- Secure workflows against common risks
- Apply security best practices for enterprise CI/CD

---

## Prerequisite

- Steps 01–16 completed
- Familiarity with secrets, environments, and deployments

---

## New Concepts Introduced

This step introduces exactly these concepts:

- **GITHUB_TOKEN and permissions**
- **Least privilege for workflows**
- **Security hardening best practices**

No OIDC, no cloud IAM yet.

---

## The Default Security Model

- Every workflow gets a `GITHUB_TOKEN`
- Token is scoped to the repository
- Used for:
  - Checking out code
  - Creating releases
  - Updating PRs and issues

Risk:
- Over-privileged tokens increase blast radius

---

## What Is Least Privilege?

Principle:
- Grant **only the permissions required**
- Remove everything else

Benefits:
- Reduced attack surface
- Safer CI pipelines
- Compliance-friendly (BFSI, enterprise)

---

## Controlling Permissions

Permissions can be set at:

- Workflow level
- Job level (overrides workflow)

---

## Common Permission Scopes

- `contents` – read/write repository files
- `pull-requests` – comment or update PRs
- `issues` – create or modify issues
- `packages` – publish packages
- `id-token` – OIDC (later topic)

---

## Workflow-Level Permissions (Recommended)

```yaml
permissions:
  contents: read
```

Meaning:
- Workflow can read repo contents
- Cannot write or modify anything

---

## Job-Level Override

```yaml
jobs:
  deploy:
    permissions:
      contents: read
      deployments: write
```

Used only when elevated access is required.

---

## Workflow File: `17-security-and-permissions.yml`

> Paste inside a YAML code block

```yaml
name: 17 - Security and Permissions

on:
  push:
    branches:
      - main
  workflow_dispatch:

permissions:
  contents: read

jobs:
  secure-job:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout repository
        uses: actions/checkout@v4

      - name: Safe operation
        run: echo "Running with read-only permissions"
```

---

## What Happens If Permissions Are Insufficient

- GitHub blocks the operation
- Workflow fails with permission error
- Clear error message shown in logs

This is expected and desired.

---

## Security Best Practices

- Always define `permissions:` explicitly
- Prefer `contents: read`
- Use environment approvals for production
- Never print secrets
- Pin action versions (`@v4`, not `@main`)
- Review third-party actions carefully

---

## Typical Enterprise Patterns

- Read-only CI workflows
- Separate deploy workflows with approvals
- Restricted permissions for prod jobs
- Audit-friendly pipeline design

---

## Key Takeaways

- Default tokens can be over-privileged
- Least privilege is mandatory for secure CI/CD
- Permissions are easy to control and audit
- Critical for regulated environments

---

## Validation Checklist

- Permissions explicitly defined
- Workflow runs successfully
- No unexpected access granted
- Logs show normal execution

---

**Next Step (Advanced / Cloud):**  
Step 18 – OIDC and Cloud Authentication (AWS / Azure / GCP)
