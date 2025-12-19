#!/bin/bash

# Test script for Transfer Service Mock API
# Tests all endpoints with various scenarios

BASE_URL="${BASE_URL:-http://localhost:4010/v1}"
TOKEN="Bearer mock-token-12345"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "========================================="
echo "Testing Transfer Service Mock API"
echo "Base URL: $BASE_URL"
echo "========================================="

# Function to print test result
print_result() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✅ PASS${NC}: $2"
    else
        echo -e "${RED}❌ FAIL${NC}: $2"
    fi
}

# Test counter
TOTAL_TESTS=0
PASSED_TESTS=0

# Test 1: Health Check
echo -e "\n${YELLOW}Test 1: Health Check${NC}"
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/health")
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n-1)

TOTAL_TESTS=$((TOTAL_TESTS + 1))
if [ "$HTTP_CODE" = "200" ]; then
    echo "$BODY" | jq '.'
    PASSED_TESTS=$((PASSED_TESTS + 1))
    print_result 0 "Health check endpoint"
else
    echo "HTTP Code: $HTTP_CODE"
    echo "Body: $BODY"
    print_result 1 "Health check endpoint"
fi

# Test 2: Successful Transfer
echo -e "\n${YELLOW}Test 2: Successful Transfer${NC}"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/transfers" \
  -H "Content-Type: application/json" \
  -H "Authorization: $TOKEN" \
  -d '{
    "sourceUPI": "alice@okaxis",
    "destinationUPI": "bob@paytm",
    "amount": 500.00,
    "remarks": "Test payment"
  }')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n-1)

TOTAL_TESTS=$((TOTAL_TESTS + 1))
if [ "$HTTP_CODE" = "200" ]; then
    echo "$BODY" | jq '.'
    PASSED_TESTS=$((PASSED_TESTS + 1))
    print_result 0 "Successful transfer"
else
    echo "HTTP Code: $HTTP_CODE"
    echo "Body: $BODY"
    print_result 1 "Successful transfer"
fi

# Test 3: High Value Transfer (with fee)
echo -e "\n${YELLOW}Test 3: High Value Transfer (with fee)${NC}"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/transfers" \
  -H "Content-Type: application/json" \
  -H "Authorization: $TOKEN" \
  -d '{
    "sourceUPI": "alice@okaxis",
    "destinationUPI": "bob@paytm",
    "amount": 5000.00,
    "remarks": "High value transfer"
  }')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n-1)

TOTAL_TESTS=$((TOTAL_TESTS + 1))
if [ "$HTTP_CODE" = "200" ]; then
    echo "$BODY" | jq '.'
    PASSED_TESTS=$((PASSED_TESTS + 1))
    print_result 0 "High value transfer"
else
    echo "HTTP Code: $HTTP_CODE"
    print_result 1 "High value transfer"
fi

# Test 4: Get Transaction Status
echo -e "\n${YELLOW}Test 4: Get Transaction Status${NC}"
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/transactions/TXN-20241220-123456" \
  -H "Authorization: $TOKEN")

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n-1)

TOTAL_TESTS=$((TOTAL_TESTS + 1))
if [ "$HTTP_CODE" = "200" ]; then
    echo "$BODY" | jq '.'
    PASSED_TESTS=$((PASSED_TESTS + 1))
    print_result 0 "Get transaction status"
else
    echo "HTTP Code: $HTTP_CODE"
    print_result 1 "Get transaction status"
fi

# Test 5: Get Transaction History
echo -e "\n${YELLOW}Test 5: Get Transaction History (Paginated)${NC}"
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/transactions?page=0&size=5" \
  -H "Authorization: $TOKEN")

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n-1)

TOTAL_TESTS=$((TOTAL_TESTS + 1))
if [ "$HTTP_CODE" = "200" ]; then
    echo "$BODY" | jq '.'
    PASSED_TESTS=$((PASSED_TESTS + 1))
    print_result 0 "Get transaction history"
else
    echo "HTTP Code: $HTTP_CODE"
    print_result 1 "Get transaction history"
fi

# Test 6: Filter by Status
echo -e "\n${YELLOW}Test 6: Filter Transaction History by Status${NC}"
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/transactions?status=SUCCESS&page=0&size=10" \
  -H "Authorization: $TOKEN")

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n-1)

TOTAL_TESTS=$((TOTAL_TESTS + 1))
if [ "$HTTP_CODE" = "200" ]; then
    echo "$BODY" | jq '.'
    PASSED_TESTS=$((PASSED_TESTS + 1))
    print_result 0 "Filter by status"
else
    echo "HTTP Code: $HTTP_CODE"
    print_result 1 "Filter by status"
fi

# Test 7: Invalid UPI Format (should return 400)
echo -e "\n${YELLOW}Test 7: Invalid UPI Format (Error Case)${NC}"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/transfers" \
  -H "Content-Type: application/json" \
  -H "Authorization: $TOKEN" \
  -H "Prefer: code=400, example=invalid_upi_format" \
  -d '{
    "sourceUPI": "invalid-format",
    "destinationUPI": "bob@paytm",
    "amount": 500.00
  }')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n-1)

TOTAL_TESTS=$((TOTAL_TESTS + 1))
if [ "$HTTP_CODE" = "400" ]; then
    echo "$BODY" | jq '.'
    PASSED_TESTS=$((PASSED_TESTS + 1))
    print_result 0 "Invalid UPI format validation"
else
    echo "HTTP Code: $HTTP_CODE"
    echo "Body: $BODY"
    print_result 1 "Invalid UPI format validation"
fi

# Test 8: Insufficient Balance (should return 400)
echo -e "\n${YELLOW}Test 8: Insufficient Balance (Error Case)${NC}"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/transfers" \
  -H "Content-Type: application/json" \
  -H "Authorization: $TOKEN" \
  -H "Prefer: code=400, example=insufficient_balance" \
  -d '{
    "sourceUPI": "poor@okaxis",
    "destinationUPI": "bob@paytm",
    "amount": 10000.00
  }')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n-1)

TOTAL_TESTS=$((TOTAL_TESTS + 1))
if [ "$HTTP_CODE" = "400" ]; then
    echo "$BODY" | jq '.'
    PASSED_TESTS=$((PASSED_TESTS + 1))
    print_result 0 "Insufficient balance validation"
else
    echo "HTTP Code: $HTTP_CODE"
    print_result 1 "Insufficient balance validation"
fi

# Test 9: Unauthorized (should return 401)
echo -e "\n${YELLOW}Test 9: Unauthorized Access (Error Case)${NC}"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/transfers" \
  -H "Content-Type: application/json" \
  -H "Prefer: code=401" \
  -d '{
    "sourceUPI": "alice@okaxis",
    "destinationUPI": "bob@paytm",
    "amount": 500.00
  }')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n-1)

TOTAL_TESTS=$((TOTAL_TESTS + 1))
if [ "$HTTP_CODE" = "401" ]; then
    echo "$BODY" | jq '.'
    PASSED_TESTS=$((PASSED_TESTS + 1))
    print_result 0 "Unauthorized access validation"
else
    echo "HTTP Code: $HTTP_CODE"
    print_result 1 "Unauthorized access validation"
fi

# Test 10: Rate Limiting (should return 429)
echo -e "\n${YELLOW}Test 10: Rate Limiting (Error Case)${NC}"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/transfers" \
  -H "Content-Type: application/json" \
  -H "Authorization: $TOKEN" \
  -H "Prefer: code=429" \
  -d '{
    "sourceUPI": "alice@okaxis",
    "destinationUPI": "bob@paytm",
    "amount": 500.00
  }')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n-1)

TOTAL_TESTS=$((TOTAL_TESTS + 1))
if [ "$HTTP_CODE" = "429" ]; then
    echo "$BODY" | jq '.'
    PASSED_TESTS=$((PASSED_TESTS + 1))
    print_result 0 "Rate limiting validation"
else
    echo "HTTP Code: $HTTP_CODE"
    print_result 1 "Rate limiting validation"
fi

# Summary
echo -e "\n========================================="
echo -e "${YELLOW}Test Summary${NC}"
echo "========================================="
echo -e "Total Tests:  $TOTAL_TESTS"
echo -e "Passed:       ${GREEN}$PASSED_TESTS${NC}"
echo -e "Failed:       ${RED}$((TOTAL_TESTS - PASSED_TESTS))${NC}"
echo -e "Success Rate: $((PASSED_TESTS * 100 / TOTAL_TESTS))%"
echo "========================================="

# Exit with appropriate code
if [ $PASSED_TESTS -eq $TOTAL_TESTS ]; then
    echo -e "${GREEN}✅ All tests passed!${NC}"
    exit 0
else
    echo -e "${RED}❌ Some tests failed!${NC}"
    exit 1
fi
