# Level 2: Quick Start Guide

## 🚀 Get Started in 10 Minutes

This guide helps you get the mock server and contract testing running quickly.

---

## Prerequisites

```bash
# Check installations
node --version   # v18+
npm --version    # v9+
java --version   # Java 17+
mvn --version    # Maven 3.8+
docker --version # Docker 20+
```

---

## Step 1: Start Mock Server (2 minutes)

### Option A: Using NPM (Fastest)

```bash
# Install Prism
npm install -g @stoplight/prism-cli

# Start mock server
prism mock transfer-service-api.yaml -p 4010

# ✅ Mock API running at http://localhost:4010
```

### Option B: Using Docker Compose (Recommended)

```bash
# Start all services (mock + swagger + wiremock)
docker-compose up -d

# ✅ Services running:
# - Mock API: http://localhost:4010
# - Swagger UI: http://localhost:8081
# - WireMock: http://localhost:8080
```

---

## Step 2: Test Mock Server (2 minutes)

```bash
# Test 1: Health check
curl http://localhost:4010/v1/health | jq

# Expected: {"status": "UP", ...}

# Test 2: Transfer money
curl -X POST http://localhost:4010/v1/transfers \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer test-token" \
  -d '{
    "sourceUPI": "alice@okaxis",
    "destinationUPI": "bob@paytm",
    "amount": 500.00,
    "remarks": "Test payment"
  }' | jq

# Expected: {"transactionId": "TXN-...", "status": "SUCCESS", ...}

# Test 3: Get transaction
curl http://localhost:4010/v1/transactions/TXN-20241220-123456 \
  -H "Authorization: Bearer test-token" | jq

# Test 4: Get history
curl "http://localhost:4010/v1/transactions?page=0&size=10" \
  -H "Authorization: Bearer test-token" | jq
```

### Or use the test script:

```bash
chmod +x test-mock.sh
./test-mock.sh
```

---

## Step 3: View API Documentation (1 minute)

Open Swagger UI in browser:
```
http://localhost:8081
```

You'll see:
- All endpoints documented
- Request/response schemas
- Try out API directly in browser
- Examples for each endpoint

---

## Step 4: Setup Contract Testing (5 minutes)

### Create Spring Boot Project Structure

```bash
mkdir -p transfer-service/src/{main,test}/{java,resources}
cd transfer-service
```

### Create pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    
    <groupId>com.npci</groupId>
    <artifactId>transfer-service</artifactId>
    <version>1.0.0</version>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
    </parent>
    
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-contract-verifier</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-contract-maven-plugin</artifactId>
                <version>4.1.0</version>
                <extensions>true</extensions>
                <configuration>
                    <baseClassForTests>
                        com.npci.transfer.contract.BaseContractTest
                    </baseClassForTests>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

### Copy Contract Files

```bash
# Copy contract definitions
mkdir -p src/test/resources/contracts/transfers
# Place .groovy contract files here
```

### Run Contract Tests

```bash
# Generate tests from contracts
mvn spring-cloud-contract:generateTests

# Run tests
mvn test

# Expected output:
# [INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
# [INFO] BUILD SUCCESS
```

---

## Step 5: Generate Client SDKs (Optional - 2 minutes)

### Java Client

```bash
openapi-generator-cli generate \
  -i transfer-service-api.yaml \
  -g java \
  -o ./generated-clients/java \
  --library resttemplate
```

### TypeScript Client

```bash
openapi-generator-cli generate \
  -i transfer-service-api.yaml \
  -g typescript-axios \
  -o ./generated-clients/typescript
```

### Python Client

```bash
openapi-generator-cli generate \
  -i transfer-service-api.yaml \
  -g python \
  -o ./generated-clients/python
```

---

## Common Commands

### Mock Server

```bash
# Start Prism
prism mock transfer-service-api.yaml -p 4010

# Start with dynamic responses
prism mock transfer-service-api.yaml -p 4010 --dynamic

# Start all services
docker-compose up -d

# Stop all services
docker-compose down

# View logs
docker-compose logs -f prism
```

### Contract Testing

```bash
# Generate contract tests
mvn spring-cloud-contract:generateTests

# Run tests
mvn test

# Generate stubs
mvn spring-cloud-contract:generateStubs

# Full build
mvn clean install
```

### Client Generation

```bash
# List available generators
openapi-generator-cli list

# Generate specific client
openapi-generator-cli generate -i transfer-service-api.yaml -g <generator-name> -o ./output
```

---

## Troubleshooting

### Issue: Prism not found
```bash
# Install globally
npm install -g @stoplight/prism-cli

# Or use npx
npx @stoplight/prism-cli mock transfer-service-api.yaml
```

### Issue: Port already in use
```bash
# Find process using port
lsof -i :4010

# Kill process
kill -9 <PID>

# Or use different port
prism mock transfer-service-api.yaml -p 5000
```

### Issue: Docker services not starting
```bash
# Check logs
docker-compose logs

# Restart services
docker-compose restart

# Rebuild
docker-compose up --build
```

### Issue: Contract tests failing
```bash
# Check BaseContractTest exists
# Verify contract files in correct location
# Ensure mocks are setup in BaseContractTest

# Clean and rebuild
mvn clean install
```

---

## File Structure Overview

```
level-02-api-first-design/
├── README.md                          # Overview
├── transfer-service-api.yaml          # ⭐ OpenAPI Specification
├── MOCK_SERVER_SETUP.md              # Mock server guide
├── SPRING_CLOUD_CONTRACT.md          # Contract testing guide
├── CLIENT_SDK_GENERATION.md          # SDK generation guide
├── METRICS.md                        # Success metrics
├── docker-compose.yml                # All-in-one setup
├── test-mock.sh                      # Test script
└── contracts/                        # Contract definitions
    └── transfers/
        ├── shouldInitiateTransferSuccessfully.groovy
        ├── shouldReturnInsufficientBalanceError.groovy
        └── ...
```

---

## What's Next?

After completing Level 2:

1. ✅ **Mock Server Running**: Frontend can start development
2. ✅ **API Contract Defined**: Clear interface agreement
3. ✅ **Contract Tests Ready**: Auto-verify implementation
4. ✅ **Documentation Live**: Swagger UI accessible
5. ⏳ **Level 3**: Test Data Strategy
6. ⏳ **Level 4**: Implement actual service

---

## Success Checklist

- [ ] Mock server running on port 4010
- [ ] Swagger UI accessible at port 8081
- [ ] Successfully tested all 4 endpoints with curl
- [ ] Contract tests generating and passing
- [ ] Generated at least one client SDK
- [ ] Reviewed OpenAPI specification
- [ ] Understand contract-first approach

---

## Resources

- OpenAPI Spec: `transfer-service-api.yaml`
- Mock Server: http://localhost:4010/v1
- Swagger UI: http://localhost:8081
- Contract Examples: `contracts/transfers/*.groovy`

---

## Support

If you encounter issues:
1. Check Prerequisites are installed
2. Review Troubleshooting section
3. Check docker-compose logs
4. Verify ports are available

---

**Total Setup Time**: ~10 minutes  
**Complexity**: Low  
**Dependencies**: Node.js, Java, Docker (optional)

🎉 **You're ready to go!** Mock server and contract testing are now operational.
