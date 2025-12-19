#!/bin/bash

# Run Tests and Generate Coverage Report
# Usage: ./run-tests.sh

set -e

echo "🚀 Running Comprehensive Unit Tests..."
echo "======================================"

# Run all tests with coverage
mvn clean test jacoco:report

# Check if tests passed
if [ $? -eq 0 ]; then
    echo ""
    echo "✅ All tests passed!"
    echo ""
    echo "📊 Coverage Report Generated:"
    echo "   target/site/jacoco/index.html"
    echo ""
    
    # Try to open coverage report
    if command -v open &> /dev/null; then
        echo "🌐 Opening coverage report..."
        open target/site/jacoco/index.html
    elif command -v xdg-open &> /dev/null; then
        echo "🌐 Opening coverage report..."
        xdg-open target/site/jacoco/index.html
    else
        echo "💡 Open target/site/jacoco/index.html in your browser"
    fi
else
    echo ""
    echo "❌ Tests failed!"
    exit 1
fi
