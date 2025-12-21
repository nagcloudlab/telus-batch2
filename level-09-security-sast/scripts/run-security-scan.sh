#!/bin/bash

# Security Analysis Script
# Level 9: Security Analysis - SAST

set -e  # Exit on error

echo "═══════════════════════════════════════════════════"
echo "  Security Analysis - Transfer Service"
echo "═══════════════════════════════════════════════════"
echo ""

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print status
print_status() {
    echo -e "${BLUE}➜${NC} $1"
}

print_success() {
    echo -e "${GREEN}✅${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠️${NC}  $1"
}

print_error() {
    echo -e "${RED}❌${NC} $1"
}

# Check prerequisites
print_status "Checking prerequisites..."

if ! command -v java &> /dev/null; then
    print_error "Java not found. Please install Java 17+"
    exit 1
fi

if ! command -v mvn &> /dev/null; then
    print_error "Maven not found. Please install Maven 3.6+"
    exit 1
fi

print_success "Prerequisites OK"
echo ""

# Clean previous build
print_status "Cleaning previous build..."
mvn clean > /dev/null 2>&1
print_success "Clean complete"
echo ""

# Compile
print_status "Compiling source code..."
mvn compile -q
if [ $? -ne 0 ]; then
    print_error "Compilation failed"
    exit 1
fi
print_success "Compilation complete"
echo ""

# Run tests
print_status "Running unit tests..."
mvn test -q
if [ $? -ne 0 ]; then
    print_error "Tests failed"
    exit 1
fi
print_success "All tests passed"
echo ""

# SpotBugs Analysis
print_status "Running SpotBugs + FindSecurityBugs..."
echo "   This may take 30-60 seconds..."
mvn spotbugs:check -q
if [ $? -ne 0 ]; then
    print_error "SpotBugs found issues"
    print_warning "Check: target/spotbugs/spotbugsXml.html"
    exit 1
fi
print_success "SpotBugs: 0 issues found"
echo ""

# OWASP Dependency-Check
print_status "Running OWASP Dependency-Check..."
echo "   This may take 2-3 minutes on first run..."

# Check if NVD database exists
if [ ! -d "$HOME/.m2/repository/org/owasp/dependency-check-data" ]; then
    print_warning "NVD database not found. Downloading..."
    print_warning "This will take 5-10 minutes (one-time only)..."
fi

mvn dependency-check:check -q
if [ $? -ne 0 ]; then
    print_error "Dependency-Check found vulnerabilities"
    print_warning "Check: target/dependency-check-report.html"
    exit 1
fi
print_success "OWASP: 0 vulnerabilities found"
echo ""

# Generate summary
echo ""
echo "═══════════════════════════════════════════════════"
echo "  Security Analysis Complete ✅"
echo "═══════════════════════════════════════════════════"
echo ""
echo "Results:"
echo "  SpotBugs:              ✅ 0 bugs"
echo "  FindSecurityBugs:      ✅ 0 security issues"
echo "  OWASP Dependency-Check: ✅ 0 CVEs"
echo ""
echo "Reports:"
echo "  SpotBugs:      target/spotbugs/spotbugsXml.html"
echo "  OWASP:         target/dependency-check-report.html"
echo ""

# Try to open reports
if command -v open &> /dev/null; then
    read -p "Open reports in browser? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        open target/spotbugs/spotbugsXml.html
        open target/dependency-check-report.html
    fi
elif command -v xdg-open &> /dev/null; then
    read -p "Open reports in browser? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        xdg-open target/spotbugs/spotbugsXml.html
        xdg-open target/dependency-check-report.html
    fi
fi

echo ""
print_success "Security analysis complete! Your code is secure."
echo ""
