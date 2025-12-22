# GitHub Actions – Step 20C  
## Python Production CI Pipeline (Lint + Test + Coverage + Artifacts)

---

## Objective

- Build a real Python CI pipeline (production-grade baseline)
- Use official Python setup with dependency caching
- Run linting (ruff) and formatting check (optional)
- Run unit tests with pytest
- Generate coverage and upload reports as artifacts
- Run on push + pull requests

---

## Prerequisite

- Python project with one of these dependency styles:
  - `requirements.txt`
  - `pyproject.toml` (Poetry/uv/pip-tools/etc.)
- Recommended tooling:
  - `pytest`
  - `pytest-cov`
  - `ruff` (lint)

---

## Pipeline Stages

- Checkout code
- Setup Python (versioned)
- Install dependencies
- Lint (ruff)
- Unit tests (pytest)
- Coverage reports (HTML/XML)
- Upload artifacts

---

## Option A: requirements.txt (Most Common)

### Workflow File: `20C-python-ci-requirements.yml`

> Paste inside a YAML code block

```yaml
name: 20C - Python CI (requirements.txt)

on:
  push:
    branches:
      - main
      - develop
      - "feature/**"
  pull_request:
    branches:
      - main
      - develop
  workflow_dispatch:

permissions:
  contents: read

jobs:
  python-ci:
    runs-on: ubuntu-latest

    strategy:
      matrix:
        python: ["3.10", "3.11", "3.12"]

    steps:
      - name: Checkout repository
        uses: actions/checkout@v4

      - name: Setup Python
        uses: actions/setup-python@v5
        with:
          python-version: ${{ matrix.python }}
          cache: pip

      - name: Upgrade pip
        run: python -m pip install --upgrade pip

      - name: Install dependencies
        run: |
          pip install -r requirements.txt
          pip install ruff pytest pytest-cov

      - name: Lint (ruff)
        run: ruff check .

      - name: Run tests with coverage
        run: |
          pytest -q --maxfail=1 --disable-warnings             --cov=.             --cov-report=term-missing             --cov-report=xml             --cov-report=html

      - name: Upload coverage and test artifacts
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: python-reports-${{ matrix.python }}
          path: |
            coverage.xml
            htmlcov/**
            .pytest_cache/**
          if-no-files-found: ignore
```

---

## Option B: Poetry (pyproject.toml)

Use if your repo uses Poetry. Replace installation steps accordingly:

```yaml
- name: Install Poetry
  run: pip install poetry

- name: Install dependencies (Poetry)
  run: poetry install --no-interaction

- name: Lint (ruff via Poetry)
  run: poetry run ruff check .

- name: Tests (pytest via Poetry)
  run: poetry run pytest --cov=. --cov-report=xml --cov-report=html
```

---

## Notes and Production Considerations

### 1. Dependency Cache
- `actions/setup-python` with `cache: pip` caches pip downloads
- Works well with `requirements.txt` and most pip workflows

### 2. Linting Standard (ruff)
- Fast and commonly used in production Python repos
- Replace with `flake8` if needed

### 3. Coverage Artifacts
- `coverage.xml` for CI tools
- `htmlcov/` for human-friendly coverage report

---

## Expected Output

- CI runs on push and PR
- Matrix runs Python 3.10/3.11/3.12
- Artifacts include coverage outputs per Python version

---

## Common Troubleshooting

### Import errors in tests
- Ensure your package is installable or set `PYTHONPATH`
- Consider `pip install -e .` if using a package layout

### ruff not found
- Ensure installed in dependency step
- Or add it to requirements / pyproject

---

## Next Enhancements (Optional Steps)

- Add type checking (mypy)
- Add security scan (bandit)
- Add packaging and publishing (PyPI)
- Add deployment with environments and approvals

---

End of Step 20C
