#!/bin/bash

# Run All Security Scans (SAST)
# Usage: ./run-security-scan.sh

set -e

echo "🔒 Running Security Analysis (SAST)..."
echo "======================================"
echo ""

START_TIME=$(date +%s)

# ========== 1. SPOTBUGS + FIND SECURITY BUGS ==========
echo "🐛 1/3 Running SpotBugs with Find Security Bugs..."
echo "   Scanning for: SQL injection, XSS, weak crypto, hardcoded passwords..."
mvn spotbugs:check

if [ $? -eq 0 ]; then
    echo "✅ SpotBugs Security Scan PASSED (No security bugs found)"
else
    echo "❌ SpotBugs Security Scan FAILED (Security vulnerabilities detected!)"
    echo "   Check: target/spotbugsXml.xml for details"
    exit 1
fi

echo ""

# ========== 2. OWASP DEPENDENCY-CHECK ==========
echo "📦 2/3 Running OWASP Dependency-Check..."
echo "   Checking for known vulnerabilities in dependencies (CVE database)..."
echo "   ⏳ First run downloads CVE database (takes 2-5 minutes)..."

mvn org.owasp:dependency-check-maven:check

if [ $? -eq 0 ]; then
    echo "✅ OWASP Dependency-Check PASSED (No vulnerable dependencies)"
else
    echo "❌ OWASP Dependency-Check FAILED (Vulnerable dependencies detected!)"
    echo "   Check: target/dependency-check-report.html"
    exit 1
fi

echo ""

# ========== 3. SECRET SCANNING ==========
echo "🔑 3/3 Running Secret Scanning..."

# Check if git-secrets is installed
if command -v git-secrets &> /dev/null; then
    echo "   Scanning for hardcoded secrets, API keys, passwords..."
    
    # Scan entire repository
    git secrets --scan --recursive
    
    if [ $? -eq 0 ]; then
        echo "✅ Secret Scan PASSED (No secrets found)"
    else
        echo "❌ Secret Scan FAILED (Secrets detected in code!)"
        exit 1
    fi
else
    echo "⚠️  git-secrets not installed. Skipping secret scan."
    echo "   Install: brew install git-secrets (Mac) or apt-get install git-secrets (Linux)"
    echo "   Then run: git secrets --install"
fi

echo ""

# ========== SUMMARY ==========
END_TIME=$(date +%s)
DURATION=$((END_TIME - START_TIME))

echo "======================================"
echo "🎉 SECURITY SCANS COMPLETE!"
echo "======================================"
echo ""
echo "⏱️  Total Time: ${DURATION} seconds"
echo ""
echo "📊 Reports Generated:"
echo "   SpotBugs:        target/spotbugsXml.xml"
echo "   SpotBugs HTML:   target/spotbugs.html"
echo "   OWASP HTML:      target/dependency-check-report.html"
echo "   OWASP JSON:      target/dependency-check-report.json"
echo ""

# Try to open reports
if command -v open &> /dev/null; then
    echo "🌐 Opening security reports..."
    open target/spotbugs.html
    open target/dependency-check-report.html
elif command -v xdg-open &> /dev/null; then
    echo "🌐 Opening security reports..."
    xdg-open target/spotbugs.html &
    xdg-open target/dependency-check-report.html &
else
    echo "💡 Open reports manually from target/"
fi

echo ""
echo "✨ All security checks passed! Code is secure! 🔒"
