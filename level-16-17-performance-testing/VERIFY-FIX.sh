#!/bin/bash

echo "================================================"
echo "Level 13 Contract Fix Verification"
echo "================================================"
echo ""

# Check for containing() - should NOT exist
if grep -r "containing(" src/test/resources/contracts/ 2>/dev/null; then
    echo "❌ FAIL: Found 'containing()' in contracts"
    echo "   You have the OLD BROKEN version!"
    echo ""
    echo "   Solution: Re-download level-13-contract-testing-FIXED.zip"
    exit 1
else
    echo "✅ PASS: No 'containing()' found in contracts"
fi

# Check for regex() - should exist
if grep -r "regex(" src/test/resources/contracts/ > /dev/null 2>&1; then
    echo "✅ PASS: Found 'regex()' in contracts (correct!)"
else
    echo "❌ FAIL: No 'regex()' found"
    exit 1
fi

echo ""
echo "================================================"
echo "✅ ALL CHECKS PASSED - You have the FIXED version!"
echo "================================================"
echo ""
echo "Run: mvn clean test"
echo "Expected: Tests run: 116, Failures: 0, Errors: 0"
