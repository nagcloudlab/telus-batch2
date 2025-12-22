# GitHub Actions – Trainer Index  
## Complete Zero → Enterprise Curriculum (Steps 01–19)

---

## How to Use This Index

- Each step is **one concept + one workflow**
- Steps are incremental and trainer-ready
- All files are Markdown and Notion-friendly
- Recommended teaching order is sequential

---

## Foundations

### Step 01 – Hello World Workflow  
**Concepts:** Workflow, Event, Job, Step, Runner

### Step 02 – Checkout Repository Code  
**Concepts:** `uses:`, `actions/checkout`

### Step 03 – Run Commands on Repo Code  
**Concepts:** Runner working directory, command execution

### Step 04 – Environment Variables & Secrets  
**Concepts:** `env`, secrets, precedence

### Step 05 – Conditional Execution  
**Concepts:** `if:` at job and step level

---

## Core CI Orchestration

### Step 06 – Job Dependencies  
**Concepts:** `needs:` and execution order

### Step 07 – Matrix Strategy  
**Concepts:** `strategy.matrix`, parallel jobs

### Step 08 – Dependency Caching  
**Concepts:** `actions/cache`, cache keys

### Step 09 – Artifacts  
**Concepts:** upload/download artifacts, job data sharing

### Step 10 – Status, Failures & Debugging  
**Concepts:** job status, logs, failure propagation

---

## Enterprise CI/CD

### Step 11 – Environments & Manual Approvals  
**Concepts:** environments, approvals, protected deploys

### Step 12 – Reusable Workflows  
**Concepts:** `workflow_call`, job reuse

### Step 13 – Composite Actions  
**Concepts:** step-level reuse, custom actions

### Step 14 – Expressions & Contexts  
**Concepts:** `${{ }}`, `github`, `env`, `needs`, `matrix`

### Step 15 – Outputs Between Jobs  
**Concepts:** `$GITHUB_OUTPUT`, job outputs

---

## Advanced & Production-Grade

### Step 16 – Services & Containers  
**Concepts:** service containers, health checks

### Step 17 – Security & Permissions  
**Concepts:** `GITHUB_TOKEN`, least privilege

### Step 18 – OIDC & Cloud Authentication  
**Concepts:** short-lived credentials, secretless CI

### Step 19 – Enterprise CI/CD Capstone  
**Concepts:** end-to-end pipeline (Build → Test → Scan → Deploy)

---

## Suggested Next Tracks

- Step 20A – Java/Maven Production Pipeline
- Step 20B – Node.js Production Pipeline
- Step 20C – Python Production Pipeline
- BFSI-style CI/CD Reference Architecture
- Interview Q&A and Anti-Patterns

---

## Trainer Notes

- Avoid skipping steps for beginners
- Each step can be a 30–45 min session
- Combine Steps 01–05 as a single workshop if needed
- Steps 11–19 are ideal for senior engineers & DevOps

---

End of Index
