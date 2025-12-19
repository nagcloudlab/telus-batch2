# Client SDK Generation from OpenAPI

## Purpose
Auto-generate client libraries in multiple languages from OpenAPI specification to ensure consistency and reduce manual coding errors.

---

## Swagger Codegen / OpenAPI Generator

### Installation

```bash
# Install OpenAPI Generator CLI
npm install @openapitools/openapi-generator-cli -g

# Or use Docker
docker pull openapitools/openapi-generator-cli
```

---

## Generate Java Client

### Maven Plugin (Recommended)

Add to `pom.xml`:

```xml
<plugin>
    <groupId>org.openapitools</groupId>
    <artifactId>openapi-generator-maven-plugin</artifactId>
    <version>7.2.0</version>
    <executions>
        <execution>
            <goals>
                <goal>generate</goal>
            </goals>
            <configuration>
                <inputSpec>${project.basedir}/transfer-service-api.yaml</inputSpec>
                <generatorName>java</generatorName>
                <library>resttemplate</library>
                <apiPackage>com.npci.transfer.api</apiPackage>
                <modelPackage>com.npci.transfer.model</modelPackage>
                <invokerPackage>com.npci.transfer.client</invokerPackage>
                <configOptions>
                    <dateLibrary>java8</dateLibrary>
                    <java8>true</java8>
                    <useJakartaEe>true</useJakartaEe>
                </configOptions>
            </configuration>
        </execution>
    </executions>
</plugin>
```

Generate:
```bash
mvn clean compile
```

### CLI Command

```bash
openapi-generator-cli generate \
  -i transfer-service-api.yaml \
  -g java \
  -o ./generated-clients/java \
  --library resttemplate \
  --api-package com.npci.transfer.api \
  --model-package com.npci.transfer.model \
  --invoker-package com.npci.transfer.client \
  --additional-properties=dateLibrary=java8,java8=true
```

### Usage Example

```java
import com.npci.transfer.client.ApiClient;
import com.npci.transfer.api.TransfersApi;
import com.npci.transfer.model.TransferRequest;
import com.npci.transfer.model.TransferResponse;

public class TransferClientExample {
    public static void main(String[] args) {
        // Setup client
        ApiClient client = new ApiClient();
        client.setBasePath("https://api.npci.org.in/v1");
        client.setBearerToken("your-jwt-token");
        
        TransfersApi api = new TransfersApi(client);
        
        // Create transfer request
        TransferRequest request = new TransferRequest()
            .sourceUPI("alice@okaxis")
            .destinationUPI("bob@paytm")
            .amount(new BigDecimal("500.00"))
            .remarks("Lunch payment");
        
        try {
            // Initiate transfer
            TransferResponse response = api.initiateTransfer(
                UUID.randomUUID().toString(), // Idempotency-Key
                request
            );
            
            System.out.println("Transaction ID: " + response.getTransactionId());
            System.out.println("Status: " + response.getStatus());
            
        } catch (ApiException e) {
            System.err.println("Error: " + e.getResponseBody());
        }
    }
}
```

---

## Generate JavaScript/TypeScript Client

### CLI Command

```bash
openapi-generator-cli generate \
  -i transfer-service-api.yaml \
  -g typescript-axios \
  -o ./generated-clients/typescript \
  --additional-properties=npmName=@npci/transfer-client,npmVersion=1.0.0
```

### Usage Example (TypeScript)

```typescript
import { Configuration, TransfersApi, TransferRequest } from '@npci/transfer-client';

// Setup configuration
const config = new Configuration({
  basePath: 'https://api.npci.org.in/v1',
  accessToken: 'your-jwt-token'
});

const api = new TransfersApi(config);

// Initiate transfer
const request: TransferRequest = {
  sourceUPI: 'alice@okaxis',
  destinationUPI: 'bob@paytm',
  amount: 500.00,
  remarks: 'Lunch payment'
};

api.initiateTransfer(request, {
  headers: {
    'Idempotency-Key': crypto.randomUUID()
  }
})
  .then(response => {
    console.log('Transaction ID:', response.data.transactionId);
    console.log('Status:', response.data.status);
  })
  .catch(error => {
    console.error('Error:', error.response?.data);
  });
```

---

## Generate Python Client

### CLI Command

```bash
openapi-generator-cli generate \
  -i transfer-service-api.yaml \
  -g python \
  -o ./generated-clients/python \
  --additional-properties=packageName=npci_transfer_client,projectName=npci-transfer-client
```

### Usage Example

```python
from npci_transfer_client import ApiClient, Configuration
from npci_transfer_client.api import transfers_api
from npci_transfer_client.model.transfer_request import TransferRequest

# Setup configuration
config = Configuration(
    host="https://api.npci.org.in/v1",
    access_token="your-jwt-token"
)

with ApiClient(config) as api_client:
    api = transfers_api.TransfersApi(api_client)
    
    # Create transfer request
    request = TransferRequest(
        source_upi="alice@okaxis",
        destination_upi="bob@paytm",
        amount=500.00,
        remarks="Lunch payment"
    )
    
    try:
        # Initiate transfer
        response = api.initiate_transfer(
            transfer_request=request,
            idempotency_key="550e8400-e29b-41d4-a716-446655440000"
        )
        
        print(f"Transaction ID: {response.transaction_id}")
        print(f"Status: {response.status}")
        
    except Exception as e:
        print(f"Error: {e}")
```

---

## Generate Go Client

### CLI Command

```bash
openapi-generator-cli generate \
  -i transfer-service-api.yaml \
  -g go \
  -o ./generated-clients/go \
  --additional-properties=packageName=transferclient
```

### Usage Example

```go
package main

import (
    "context"
    "fmt"
    "github.com/google/uuid"
    transfer "path/to/generated/transferclient"
)

func main() {
    // Setup configuration
    config := transfer.NewConfiguration()
    config.Servers = transfer.ServerConfigurations{
        {URL: "https://api.npci.org.in/v1"},
    }
    config.AddDefaultHeader("Authorization", "Bearer your-jwt-token")
    
    client := transfer.NewAPIClient(config)
    
    // Create transfer request
    request := transfer.TransferRequest{
        SourceUPI:      "alice@okaxis",
        DestinationUPI: "bob@paytm",
        Amount:         500.00,
        Remarks:        transfer.PtrString("Lunch payment"),
    }
    
    // Initiate transfer
    idempotencyKey := uuid.New().String()
    response, httpRes, err := client.TransfersApi.InitiateTransfer(context.Background()).
        IdempotencyKey(idempotencyKey).
        TransferRequest(request).
        Execute()
    
    if err != nil {
        fmt.Printf("Error: %v\n", err)
        return
    }
    
    fmt.Printf("Transaction ID: %s\n", response.GetTransactionId())
    fmt.Printf("Status: %s\n", response.GetStatus())
}
```

---

## Generate Postman Collection

### CLI Command

```bash
openapi-generator-cli generate \
  -i transfer-service-api.yaml \
  -g postman-collection \
  -o ./generated-clients/postman
```

### Import to Postman

1. Open Postman
2. Click "Import"
3. Select generated `transfer-service-api.postman_collection.json`
4. Setup environment variables:
   - `base_url`: https://api.npci.org.in/v1
   - `auth_token`: your-jwt-token

---

## Spring Boot Integration

### Generate Spring RestTemplate Client

```bash
openapi-generator-cli generate \
  -i transfer-service-api.yaml \
  -g java \
  -o ./generated-clients/spring \
  --library resttemplate \
  --additional-properties=dateLibrary=java8,java8=true,useSpringBoot3=true
```

### Spring WebClient (Reactive)

```bash
openapi-generator-cli generate \
  -i transfer-service-api.yaml \
  -g java \
  -o ./generated-clients/spring-webclient \
  --library webclient \
  --additional-properties=dateLibrary=java8,java8=true,useSpringBoot3=true
```

---

## Validation of Generated Clients

### Test Script

Create `test-generated-clients.sh`:

```bash
#!/bin/bash

echo "Testing Generated Clients"
echo "========================="

# Test Java Client
echo -e "\n1. Testing Java Client..."
cd generated-clients/java
mvn clean test
cd ../..

# Test TypeScript Client
echo -e "\n2. Testing TypeScript Client..."
cd generated-clients/typescript
npm install
npm test
cd ../..

# Test Python Client
echo -e "\n3. Testing Python Client..."
cd generated-clients/python
pip install -e .
python -m pytest
cd ../..

echo -e "\n========================="
echo "All client tests complete"
```

---

## Benefits of Generated Clients

✅ **Consistency**: All clients follow same contract  
✅ **Type Safety**: Compile-time validation  
✅ **Auto-Updates**: Regenerate when API changes  
✅ **Reduced Errors**: No manual JSON parsing  
✅ **Documentation**: Generated docs included  
✅ **Multi-Language**: Support for 50+ languages  

---

## CI/CD Integration

### GitHub Actions Workflow

```yaml
name: Generate and Publish Clients

on:
  push:
    paths:
      - 'transfer-service-api.yaml'

jobs:
  generate-clients:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup Java
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          
      - name: Setup Node
        uses: actions/setup-node@v3
        with:
          node-version: '18'
      
      - name: Install OpenAPI Generator
        run: npm install -g @openapitools/openapi-generator-cli
      
      - name: Generate Java Client
        run: |
          openapi-generator-cli generate \
            -i transfer-service-api.yaml \
            -g java \
            -o ./generated-clients/java
      
      - name: Generate TypeScript Client
        run: |
          openapi-generator-cli generate \
            -i transfer-service-api.yaml \
            -g typescript-axios \
            -o ./generated-clients/typescript
      
      - name: Publish Java Client to Maven
        working-directory: ./generated-clients/java
        run: mvn deploy
        
      - name: Publish TypeScript Client to NPM
        working-directory: ./generated-clients/typescript
        run: |
          npm install
          npm publish
```

---

## Next Steps

1. ✅ Generate clients for required languages
2. ✅ Test generated clients against mock server
3. ✅ Publish clients to package repositories
4. ⏳ Setup CI/CD for automatic client generation
5. ⏳ Integrate clients in consumer applications
