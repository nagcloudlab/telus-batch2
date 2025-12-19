# Mock Server Setup Guide

## Purpose
Set up a mock server from OpenAPI specification to enable parallel development and testing without actual backend implementation.

---

## Option 1: Prism Mock Server (Recommended)

### Installation

```bash
# Install Prism globally
npm install -g @stoplight/prism-cli

# Verify installation
prism --version
```

### Start Mock Server

```bash
# Start mock server on port 4010
prism mock transfer-service-api.yaml -p 4010

# With dynamic response generation
prism mock transfer-service-api.yaml -p 4010 --dynamic

# With CORS enabled
prism mock transfer-service-api.yaml -p 4010 --cors
```

### Test Mock Server

```bash
# Health check (no auth required)
curl http://localhost:4010/v1/health

# Transfer request (mock will return example response)
curl -X POST http://localhost:4010/v1/transfers \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer mock-token" \
  -d '{
    "sourceUPI": "alice@okaxis",
    "destinationUPI": "bob@paytm",
    "amount": 500.00,
    "remarks": "Test transfer"
  }'

# Get transaction status
curl http://localhost:4010/v1/transactions/TXN-20241220-123456 \
  -H "Authorization: Bearer mock-token"

# Get transaction history
curl "http://localhost:4010/v1/transactions?page=0&size=10" \
  -H "Authorization: Bearer mock-token"
```

### Expected Mock Responses

**POST /v1/transfers (Success)**
```json
{
  "transactionId": "TXN-20241220-123456",
  "status": "SUCCESS",
  "sourceUPI": "alice@okaxis",
  "destinationUPI": "bob@paytm",
  "amount": 500.00,
  "fee": 0.00,
  "totalDebited": 500.00,
  "timestamp": "2024-12-20T10:30:45.123Z",
  "remarks": "Lunch payment"
}
```

**POST /v1/transfers (400 - Insufficient Balance)**
```bash
# Request invalid amount to trigger 400
curl -X POST http://localhost:4010/v1/transfers \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer mock-token" \
  -H "Prefer: code=400, example=insufficient_balance" \
  -d '{
    "sourceUPI": "alice@okaxis",
    "destinationUPI": "bob@paytm",
    "amount": 500.00
  }'
```

Response:
```json
{
  "errorCode": "INSUFFICIENT_BALANCE",
  "message": "Insufficient balance. Available: ₹100, Required: ₹500",
  "timestamp": "2024-12-20T10:30:45.123Z"
}
```

---

## Option 2: WireMock

### Installation

```bash
# Download WireMock standalone
wget https://repo1.maven.org/maven2/org/wiremock/wiremock-standalone/3.3.1/wiremock-standalone-3.3.1.jar

# Or use Docker
docker pull wiremock/wiremock:latest
```

### Start WireMock

```bash
# Standalone
java -jar wiremock-standalone-3.3.1.jar --port 8080

# Docker
docker run -it --rm \
  -p 8080:8080 \
  -v $(pwd)/wiremock:/home/wiremock \
  wiremock/wiremock:latest
```

### Create Stub Mappings

Create `wiremock/mappings/transfer-success.json`:

```json
{
  "request": {
    "method": "POST",
    "url": "/v1/transfers",
    "headers": {
      "Content-Type": {
        "equalTo": "application/json"
      }
    },
    "bodyPatterns": [
      {
        "matchesJsonPath": "$.sourceUPI"
      },
      {
        "matchesJsonPath": "$.destinationUPI"
      }
    ]
  },
  "response": {
    "status": 200,
    "headers": {
      "Content-Type": "application/json"
    },
    "jsonBody": {
      "transactionId": "TXN-20241220-123456",
      "status": "SUCCESS",
      "sourceUPI": "{{jsonPath request.body '$.sourceUPI'}}",
      "destinationUPI": "{{jsonPath request.body '$.destinationUPI'}}",
      "amount": "{{jsonPath request.body '$.amount'}}",
      "fee": 0.00,
      "totalDebited": "{{jsonPath request.body '$.amount'}}",
      "timestamp": "{{now format='yyyy-MM-dd'T'HH:mm:ss.SSS'Z'}}",
      "remarks": "{{jsonPath request.body '$.remarks'}}"
    },
    "transformers": ["response-template"]
  }
}
```

---

## Option 3: Swagger UI with Mock

### Using Docker

```bash
# Run Swagger UI with OpenAPI spec
docker run -p 8081:8080 \
  -e SWAGGER_JSON=/api/transfer-service-api.yaml \
  -v $(pwd):/api \
  swaggerapi/swagger-ui
```

Access at: http://localhost:8081

### Using NPM

```bash
# Install swagger-ui-express
npm install swagger-ui-express express yamljs

# Create server.js
```

```javascript
const express = require('express');
const swaggerUi = require('swagger-ui-express');
const YAML = require('yamljs');

const app = express();
const swaggerDocument = YAML.load('./transfer-service-api.yaml');

app.use('/api-docs', swaggerUi.serve, swaggerUi.setup(swaggerDocument));

app.listen(3000, () => {
  console.log('Swagger UI available at http://localhost:3000/api-docs');
});
```

```bash
node server.js
```

---

## Docker Compose Setup (All-in-One)

Create `docker-compose.yml`:

```yaml
version: '3.8'

services:
  prism:
    image: stoplight/prism:latest
    container_name: transfer-service-mock
    ports:
      - "4010:4010"
    volumes:
      - ./transfer-service-api.yaml:/api/transfer-service-api.yaml
    command: mock -h 0.0.0.0 -p 4010 --dynamic /api/transfer-service-api.yaml

  swagger-ui:
    image: swaggerapi/swagger-ui
    container_name: transfer-service-docs
    ports:
      - "8081:8080"
    volumes:
      - ./transfer-service-api.yaml:/api/transfer-service-api.yaml
    environment:
      SWAGGER_JSON: /api/transfer-service-api.yaml
      BASE_URL: /api-docs

  wiremock:
    image: wiremock/wiremock:latest
    container_name: transfer-service-wiremock
    ports:
      - "8080:8080"
    volumes:
      - ./wiremock:/home/wiremock
```

Start all services:
```bash
docker-compose up -d
```

Access:
- Mock API (Prism): http://localhost:4010/v1
- API Docs (Swagger): http://localhost:8081
- WireMock: http://localhost:8080

---

## Testing Mock Servers

### Test Script (test-mock.sh)

```bash
#!/bin/bash

BASE_URL="http://localhost:4010/v1"
TOKEN="Bearer mock-token-12345"

echo "========================================="
echo "Testing Transfer Service Mock API"
echo "========================================="

# Test 1: Health Check
echo -e "\n1. Health Check"
curl -s "$BASE_URL/health" | jq

# Test 2: Successful Transfer
echo -e "\n2. Successful Transfer"
curl -s -X POST "$BASE_URL/transfers" \
  -H "Content-Type: application/json" \
  -H "Authorization: $TOKEN" \
  -d '{
    "sourceUPI": "alice@okaxis",
    "destinationUPI": "bob@paytm",
    "amount": 500.00,
    "remarks": "Test payment"
  }' | jq

# Test 3: Get Transaction Status
echo -e "\n3. Get Transaction Status"
curl -s "$BASE_URL/transactions/TXN-20241220-123456" \
  -H "Authorization: $TOKEN" | jq

# Test 4: Get Transaction History
echo -e "\n4. Get Transaction History"
curl -s "$BASE_URL/transactions?page=0&size=5" \
  -H "Authorization: $TOKEN" | jq

# Test 5: Invalid UPI Format (400)
echo -e "\n5. Invalid UPI Format (400)"
curl -s -X POST "$BASE_URL/transfers" \
  -H "Content-Type: application/json" \
  -H "Authorization: $TOKEN" \
  -H "Prefer: code=400, example=invalid_upi_format" \
  -d '{
    "sourceUPI": "invalid-upi",
    "destinationUPI": "bob@paytm",
    "amount": 500.00
  }' | jq

# Test 6: Rate Limit (429)
echo -e "\n6. Rate Limit Exceeded (429)"
curl -s -X POST "$BASE_URL/transfers" \
  -H "Content-Type: application/json" \
  -H "Authorization: $TOKEN" \
  -H "Prefer: code=429" \
  -d '{
    "sourceUPI": "alice@okaxis",
    "destinationUPI": "bob@paytm",
    "amount": 500.00
  }' | jq

echo -e "\n========================================="
echo "Mock API Testing Complete"
echo "========================================="
```

Make executable and run:
```bash
chmod +x test-mock.sh
./test-mock.sh
```

---

## Integration with Frontend

### JavaScript Example

```javascript
// api-client.js
const BASE_URL = process.env.MOCK_API_URL || 'http://localhost:4010/v1';
const AUTH_TOKEN = 'Bearer mock-token-12345';

async function initiateTransfer(transferRequest) {
  const response = await fetch(`${BASE_URL}/transfers`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': AUTH_TOKEN,
    },
    body: JSON.stringify(transferRequest)
  });
  
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message);
  }
  
  return await response.json();
}

async function getTransactionStatus(transactionId) {
  const response = await fetch(`${BASE_URL}/transactions/${transactionId}`, {
    headers: {
      'Authorization': AUTH_TOKEN,
    }
  });
  
  return await response.json();
}

// Usage
const transfer = {
  sourceUPI: 'alice@okaxis',
  destinationUPI: 'bob@paytm',
  amount: 500.00,
  remarks: 'Test payment'
};

initiateTransfer(transfer)
  .then(result => console.log('Transfer successful:', result))
  .catch(error => console.error('Transfer failed:', error));
```

---

## Benefits of Mock Server

✅ **Parallel Development**: Frontend can work without waiting for backend  
✅ **Consistent Responses**: Predictable test data  
✅ **Fast Feedback**: No database setup needed  
✅ **Contract Validation**: Ensures frontend follows API contract  
✅ **Demo Ready**: Show API without full implementation  
✅ **Integration Tests**: Test against stable mock  

---

## Next Steps

1. ✅ Start mock server
2. ✅ Test all endpoints with curl/Postman
3. ⏳ Generate client SDKs from OpenAPI spec
4. ⏳ Setup Spring Cloud Contract (Level 2 - Part 2)
5. ⏳ Write contract tests
