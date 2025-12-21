#!/bin/bash

# SonarQube Quick Start Script
# Level 8: Static Analysis - First Quality Gate

set -e  # Exit on error

echo "═══════════════════════════════════════════════════"
echo "  SonarQube Analysis - Transfer Service"
echo "═══════════════════════════════════════════════════"
echo ""

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Check if SonarQube is running
echo "🔍 Checking SonarQube availability..."
if curl -s http://localhost:9000/api/system/status | grep -q "UP"; then
    echo -e "${GREEN}✅ SonarQube is running${NC}"
else
    echo -e "${YELLOW}⚠️  SonarQube not running. Starting with Docker Compose...${NC}"
    docker-compose up -d
    echo "⏳ Waiting for SonarQube to start (60 seconds)..."
    sleep 60
    
    # Check again
    if curl -s http://localhost:9000/api/system/status | grep -q "UP"; then
        echo -e "${GREEN}✅ SonarQube started successfully${NC}"
    else
        echo -e "${RED}❌ Failed to start SonarQube${NC}"
        echo "   Please check: docker logs sonarqube-transfer-service"
        exit 1
    fi
fi

echo ""
echo "🧪 Running tests and generating coverage..."
mvn clean test

if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Tests failed${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Tests passed${NC}"
echo ""

# Check for SONAR_TOKEN environment variable
if [ -z "$SONAR_TOKEN" ]; then
    echo -e "${YELLOW}⚠️  SONAR_TOKEN not set${NC}"
    echo ""
    echo "Please provide your SonarQube token:"
    echo "  1. Login to http://localhost:9000"
    echo "  2. Go to: My Account → Security → Generate Token"
    echo "  3. Copy the token and paste below"
    echo ""
    read -p "Enter SonarQube token: " SONAR_TOKEN
    export SONAR_TOKEN
fi

echo ""
echo "📊 Running SonarQube analysis..."
mvn sonar:sonar -Dsonar.token=$SONAR_TOKEN

if [ $? -ne 0 ]; then
    echo -e "${RED}❌ SonarQube analysis failed${NC}"
    exit 1
fi

echo ""
echo -e "${GREEN}✅ SonarQube analysis complete!${NC}"
echo ""
echo "═══════════════════════════════════════════════════"
echo "📊 View Results:"
echo "═══════════════════════════════════════════════════"
echo ""
echo "Dashboard: http://localhost:9000/dashboard?id=transfer-service"
echo ""

# Try to open browser (works on macOS and most Linux)
if command -v open &> /dev/null; then
    echo "🌐 Opening dashboard in browser..."
    open "http://localhost:9000/dashboard?id=transfer-service"
elif command -v xdg-open &> /dev/null; then
    echo "🌐 Opening dashboard in browser..."
    xdg-open "http://localhost:9000/dashboard?id=transfer-service"
else
    echo "Please open: http://localhost:9000/dashboard?id=transfer-service"
fi

echo ""
echo -e "${GREEN}✨ Analysis complete! Check the dashboard for results.${NC}"
echo ""
