#!/bin/bash

# Run All Static Analysis Tools
# Usage: ./run-static-analysis.sh [--fail-fast]

set -e

FAIL_FAST=false

if [ "$1" == "--fail-fast" ]; then
    FAIL_FAST=true
fi

echo "🔍 Running Static Analysis Tools..."
echo "===================================="
echo ""

# ========== 1. CHECKSTYLE ==========
echo "📏 1/4 Running Checkstyle (Coding Standards)..."
mvn checkstyle:check

if [ $? -eq 0 ]; then
    echo "✅ Checkstyle PASSED"
else
    echo "❌ Checkstyle FAILED"
    if [ "$FAIL_FAST" = true ]; then
        exit 1
    fi
fi

echo ""

# ========== 2. PMD ==========
echo "🔎 2/4 Running PMD (Code Quality)..."
mvn pmd:check pmd:cpd-check

if [ $? -eq 0 ]; then
    echo "✅ PMD PASSED"
else
    echo "❌ PMD FAILED"
    if [ "$FAIL_FAST" = true ]; then
        exit 1
    fi
fi

echo ""

# ========== 3. SPOTBUGS ==========
echo "🐛 3/4 Running SpotBugs (Bug Detection)..."
mvn spotbugs:check

if [ $? -eq 0 ]; then
    echo "✅ SpotBugs PASSED"
else
    echo "❌ SpotBugs FAILED"
    if [ "$FAIL_FAST" = true ]; then
        exit 1
    fi
fi

echo ""

# ========== 4. TESTS + COVERAGE ==========
echo "🧪 4/4 Running Tests with Coverage..."
mvn clean test jacoco:report

if [ $? -eq 0 ]; then
    echo "✅ Tests PASSED"
else
    echo "❌ Tests FAILED"
    if [ "$FAIL_FAST" = true ]; then
        exit 1
    fi
fi

echo ""
echo "======================================"
echo "📊 ANALYSIS COMPLETE!"
echo "======================================"
echo ""
echo "📁 Reports Generated:"
echo "   Checkstyle: target/site/checkstyle.html"
echo "   PMD:        target/site/pmd.html"
echo "   SpotBugs:   target/site/spotbugs.html"
echo "   Coverage:   target/site/jacoco/index.html"
echo ""

# Try to open reports
if command -v open &> /dev/null; then
    echo "🌐 Opening reports..."
    open target/site/checkstyle.html
    open target/site/pmd.html
    open target/site/spotbugs.html
    open target/site/jacoco/index.html
elif command -v xdg-open &> /dev/null; then
    echo "🌐 Opening reports..."
    xdg-open target/site/checkstyle.html &
    xdg-open target/site/pmd.html &
    xdg-open target/site/spotbugs.html &
    xdg-open target/site/jacoco/index.html &
else
    echo "💡 Open reports manually from target/site/"
fi

echo ""
echo "✨ All quality checks complete!"
