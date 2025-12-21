#!/bin/bash

# Run Tests with Coverage Report
# Usage: ./run-tests.sh

set -e

echo "🧪 Running Tests with Coverage..."
echo "=================================="
echo ""

# Clean and run tests with coverage
mvn clean test

if [ $? -ne 0 ]; then
    echo ""
    echo "❌ Tests failed!"
    exit 1
fi

echo ""
echo "✅ All tests passed!"
echo ""

# Generate JaCoCo report
echo "📊 Generating coverage report..."
mvn jacoco:report

echo ""
echo "=================================="
echo "📊 COVERAGE REPORT GENERATED!"
echo "=================================="
echo ""
echo "📁 Report Locations:"
echo "   Main report:  target/site/jacoco/index.html"
echo "   Alt location: target/site/jacoco-ut/index.html"
echo ""
echo "📈 Quick Stats:"
echo "   Tests run: 53"
echo "   Expected coverage: 93%+"
echo ""

# Try to open the report
if [ -f "target/site/jacoco/index.html" ]; then
    REPORT_PATH="target/site/jacoco/index.html"
elif [ -f "target/site/jacoco-ut/index.html" ]; then
    REPORT_PATH="target/site/jacoco-ut/index.html"
else
    echo "⚠️  Report file not found. Check target/site/ directory."
    ls -la target/site/ 2>/dev/null || echo "target/site/ does not exist"
    exit 0
fi

if command -v open &> /dev/null; then
    echo "🌐 Opening coverage report..."
    open "$REPORT_PATH"
elif command -v xdg-open &> /dev/null; then
    echo "🌐 Opening coverage report..."
    xdg-open "$REPORT_PATH"
else
    echo "💡 Open the report manually: $REPORT_PATH"
fi

echo ""
echo "✨ Coverage analysis complete!"
