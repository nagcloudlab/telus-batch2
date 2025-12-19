#!/bin/bash

# Script to run TDD validator tests and generate coverage report

echo "========================================="
echo "Running TDD Validator Tests"
echo "========================================="

# Clean and run tests
mvn clean test

# Check if tests passed
if [ $? -eq 0 ]; then
    echo ""
    echo "✅ All tests passed!"
    echo ""
    
    # Generate coverage report
    echo "Generating code coverage report..."
    mvn jacoco:report
    
    echo ""
    echo "========================================="
    echo "Coverage Report Generated"
    echo "========================================="
    echo "Open: target/site/jacoco/index.html"
    echo ""
    
    # Try to open report automatically
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS
        open target/site/jacoco/index.html
    elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
        # Linux
        xdg-open target/site/jacoco/index.html 2>/dev/null || echo "Please open target/site/jacoco/index.html manually"
    fi
else
    echo ""
    echo "❌ Tests failed!"
    exit 1
fi
