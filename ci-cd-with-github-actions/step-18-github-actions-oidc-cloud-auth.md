# GitHub Actions – Step 18  
## OIDC and Cloud Authentication (No Long-Lived Secrets)

---

## Objective

- Understand why long-lived cloud secrets are risky
- Learn OpenID Connect (OIDC) basics in GitHub Actions
- Authenticate to cloud providers without storing secrets
- Apply least-privilege, short-lived credentials

---

## Prerequisite

- Steps 01–17 completed
- Understanding of secrets, permissions, and environments
- Basic cloud knowledge (AWS/Azure/GCP concepts)

---

## New Concepts Introduced

This step introduces exactly these concepts:

- **OIDC (OpenID Connect) with GitHub Actions**
- **Short-lived cloud credentials**
- **Cloud trust relationship**

No Terraform, no deployment tooling yet.

---

## The Problem with Long-Lived Secrets

Traditional approach:
- Store cloud access keys as GitHub Secrets
- Keys are long-lived
- Risky if leaked
- Hard to rotate

Security risk:
- High blast radius
- Compliance issues

---

## What Is OIDC?

- OpenID Connect is an identity protocol
- GitHub acts as an **identity provider**
- Cloud provider trusts GitHub-issued tokens
- Tokens are **short-lived** and **scoped**

Key idea:
```
No stored cloud secrets
Only temporary identity-based access
```

---

## How OIDC Works (High Level)

```text
Workflow starts
↓
GitHub issues short-lived OIDC token
↓
Cloud provider validates token
↓
Temporary cloud credentials issued
↓
Job performs cloud operation
↓
Credentials expire automatically
```

---

## Required GitHub Permissions

OIDC requires this permission:

```yaml
permissions:
  id-token: write
  contents: read
```

Without `id-token: write`, OIDC will fail.

---

## Example: AWS Authentication with OIDC

> Conceptual example (no real deployment)

### Workflow Snippet

```yaml
name: 18 - OIDC Authentication (AWS Example)

on:
  workflow_dispatch:

permissions:
  id-token: write
  contents: read

jobs:
  oidc-demo:
    runs-on: ubuntu-latest
    steps:
      - name: Configure AWS credentials via OIDC
        uses: aws-actions/configure-aws-credentials@v4
        with:
          role-to-assume: arn:aws:iam::123456789012:role/github-actions-role
          aws-region: ap-south-1

      - name: Verify identity
        run: aws sts get-caller-identity
```

---

## What Happens Internally

- GitHub issues an OIDC token
- AWS validates token against trust policy
- Temporary role credentials are generated
- No AWS access keys are stored in GitHub

---

## Cloud-Side Setup (High Level)

Each cloud requires:
- Trust relationship with GitHub
- Conditions based on:
  - Repository
  - Branch
  - Environment

Examples:
- AWS IAM Role with OIDC provider
- Azure Federated Credential
- GCP Workload Identity Federation

---

## Security Benefits

- No long-lived secrets
- Automatic credential rotation
- Scoped access (repo, branch, env)
- Strong audit trail

---

## Typical Enterprise Use Cases

- Secure production deployments
- Compliance-heavy environments
- Multi-account cloud access
- Zero-trust CI/CD pipelines

---

## Key Takeaways

- OIDC removes the need for cloud secrets
- Credentials are short-lived and secure
- Requires explicit permissions
- Best practice for modern CI/CD

---

## Validation Checklist

- `id-token: write` permission is set
- Cloud trust relationship exists
- Workflow authenticates successfully
- No cloud secrets stored in GitHub

---

**Next Step (Capstone):**  
Step 19 – End-to-End Enterprise CI/CD Pipeline (Dev → Test → Deploy)
