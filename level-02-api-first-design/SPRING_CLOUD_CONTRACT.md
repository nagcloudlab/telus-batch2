# Spring Cloud Contract Setup

## Purpose
Implement contract testing to ensure provider (backend) and consumer (frontend/other services) agree on API contract.

---

## What is Contract Testing?

**Contract Testing** ensures:
- Provider implements what contract specifies
- Consumer uses API according to contract
- Breaking changes are caught before deployment
- Both teams can develop independently

**Types:**
1. **Provider-Side**: Tests that backend implements contract correctly
2. **Consumer-Side**: Tests that frontend/services use contract correctly

---

## Project Structure

```
transfer-service/
├── src/
│   ├── main/
│   │   └── java/.../TransferController.java
│   ├── test/
│   │   ├── java/.../
│   │   │   └── BaseContractTest.java
│   │   └── resources/
│   │       └── contracts/
│   │           └── transfers/
│   │               ├── shouldInitiateTransferSuccessfully.groovy
│   │               ├── shouldReturnInsufficientBalanceError.groovy
│   │               ├── shouldReturnInvalidUPIFormatError.groovy
│   │               └── shouldGetTransactionStatus.groovy
└── pom.xml
```

---

## Maven Dependencies

Add to `pom.xml`:

```xml
<properties>
    <spring-cloud-contract.version>4.1.0</spring-cloud-contract.version>
</properties>

<dependencies>
    <!-- Spring Cloud Contract Verifier -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-contract-verifier</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- REST Assured for contract tests -->
    <dependency>
        <groupId>io.rest-assured</groupId>
        <artifactId>rest-assured</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>2023.0.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<build>
    <plugins>
        <!-- Spring Cloud Contract Maven Plugin -->
        <plugin>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-contract-maven-plugin</artifactId>
            <version>${spring-cloud-contract.version}</version>
            <extensions>true</extensions>
            <configuration>
                <baseClassForTests>
                    com.npci.transfer.contract.BaseContractTest
                </baseClassForTests>
            </configuration>
        </plugin>
    </plugins>
</build>
```

---

## Contract Definitions (Groovy DSL)

### Contract 1: Successful Transfer

`src/test/resources/contracts/transfers/shouldInitiateTransferSuccessfully.groovy`

```groovy
package contracts.transfers

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Should initiate transfer successfully"
    
    request {
        method POST()
        url "/v1/transfers"
        headers {
            contentType(applicationJson())
            header("Authorization", "Bearer valid-token-12345")
        }
        body([
            sourceUPI: "alice@okaxis",
            destinationUPI: "bob@paytm",
            amount: 500.00,
            remarks: "Lunch payment"
        ])
    }
    
    response {
        status 200
        headers {
            contentType(applicationJson())
        }
        body([
            transactionId: $(regex('TXN-\\d{8}-\\d{6}')),
            status: "SUCCESS",
            sourceUPI: "alice@okaxis",
            destinationUPI: "bob@paytm",
            amount: 500.00,
            fee: 0.00,
            totalDebited: 500.00,
            timestamp: $(regex(isoDateTime())),
            remarks: "Lunch payment"
        ])
    }
}
```

### Contract 2: Insufficient Balance Error

`src/test/resources/contracts/transfers/shouldReturnInsufficientBalanceError.groovy`

```groovy
package contracts.transfers

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Should return insufficient balance error"
    
    request {
        method POST()
        url "/v1/transfers"
        headers {
            contentType(applicationJson())
            header("Authorization", "Bearer valid-token-12345")
        }
        body([
            sourceUPI: "poor@okaxis",
            destinationUPI: "bob@paytm",
            amount: 10000.00,
            remarks: "Large payment"
        ])
    }
    
    response {
        status 400
        headers {
            contentType(applicationJson())
        }
        body([
            errorCode: "INSUFFICIENT_BALANCE",
            message: $(regex('Insufficient balance.*')),
            timestamp: $(regex(isoDateTime()))
        ])
    }
}
```

### Contract 3: Invalid UPI Format Error

`src/test/resources/contracts/transfers/shouldReturnInvalidUPIFormatError.groovy`

```groovy
package contracts.transfers

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Should return invalid UPI format error"
    
    request {
        method POST()
        url "/v1/transfers"
        headers {
            contentType(applicationJson())
            header("Authorization", "Bearer valid-token-12345")
        }
        body([
            sourceUPI: "invalid-upi-format",
            destinationUPI: "bob@paytm",
            amount: 500.00
        ])
    }
    
    response {
        status 400
        headers {
            contentType(applicationJson())
        }
        body([
            errorCode: "INVALID_UPI_FORMAT",
            message: "UPI ID must be in format: username@bankcode",
            timestamp: $(regex(isoDateTime()))
        ])
    }
}
```

### Contract 4: Get Transaction Status

`src/test/resources/contracts/transfers/shouldGetTransactionStatus.groovy`

```groovy
package contracts.transfers

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Should get transaction status by ID"
    
    request {
        method GET()
        url $(regex('/v1/transactions/TXN-\\d{8}-\\d{6}'))
        headers {
            header("Authorization", "Bearer valid-token-12345")
        }
    }
    
    response {
        status 200
        headers {
            contentType(applicationJson())
        }
        body([
            transactionId: $(regex('TXN-\\d{8}-\\d{6}')),
            status: $(anyOf("SUCCESS", "FAILED", "PENDING")),
            sourceUPI: $(regex('[a-zA-Z0-9.\\-_]+@[a-zA-Z]+')),
            destinationUPI: $(regex('[a-zA-Z0-9.\\-_]+@[a-zA-Z]+')),
            amount: $(anyDouble()),
            fee: $(anyDouble()),
            totalDebited: $(anyDouble()),
            timestamp: $(regex(isoDateTime()))
        ])
    }
}
```

### Contract 5: Transaction History with Pagination

`src/test/resources/contracts/transfers/shouldGetTransactionHistory.groovy`

```groovy
package contracts.transfers

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Should get paginated transaction history"
    
    request {
        method GET()
        urlPath("/v1/transactions") {
            queryParameters {
                parameter("page", "0")
                parameter("size", "10")
            }
        }
        headers {
            header("Authorization", "Bearer valid-token-12345")
        }
    }
    
    response {
        status 200
        headers {
            contentType(applicationJson())
        }
        body([
            content: [
                [
                    transactionId: $(regex('TXN-\\d{8}-\\d{6}')),
                    status: "SUCCESS",
                    sourceUPI: "alice@okaxis",
                    destinationUPI: "bob@paytm",
                    amount: 500.00,
                    fee: 0.00,
                    totalDebited: 500.00,
                    timestamp: $(regex(isoDateTime())),
                    remarks: "Test payment"
                ]
            ],
            page: [
                number: 0,
                size: 10,
                totalElements: $(anyNumber()),
                totalPages: $(anyNumber())
            ]
        ])
    }
}
```

---

## Base Contract Test Class

`src/test/java/com/npci/transfer/contract/BaseContractTest.java`

```java
package com.npci.transfer.contract;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.npci.transfer.controller.TransferController;
import com.npci.transfer.service.TransferService;
import com.npci.transfer.model.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public abstract class BaseContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransferService transferService;

    @BeforeEach
    public void setup() {
        RestAssuredMockMvc.mockMvc(mockMvc);
        setupMocks();
    }

    private void setupMocks() {
        // Mock successful transfer
        TransferResponse successResponse = TransferResponse.builder()
            .transactionId("TXN-20241220-123456")
            .status("SUCCESS")
            .sourceUPI("alice@okaxis")
            .destinationUPI("bob@paytm")
            .amount(new BigDecimal("500.00"))
            .fee(BigDecimal.ZERO)
            .totalDebited(new BigDecimal("500.00"))
            .timestamp(LocalDateTime.now())
            .remarks("Lunch payment")
            .build();
        
        when(transferService.initiateTransfer(any(TransferRequest.class)))
            .thenReturn(successResponse);
        
        // Mock insufficient balance error
        when(transferService.initiateTransfer(
            argThat(req -> "poor@okaxis".equals(req.getSourceUPI()))))
            .thenThrow(new InsufficientBalanceException(
                "Insufficient balance. Available: ₹100, Required: ₹10000"));
        
        // Mock invalid UPI format error
        when(transferService.initiateTransfer(
            argThat(req -> !req.getSourceUPI().matches("^[a-zA-Z0-9.\\-_]+@[a-zA-Z]+$"))))
            .thenThrow(new InvalidUPIFormatException(
                "UPI ID must be in format: username@bankcode"));
        
        // Mock transaction status retrieval
        TransactionDetail transactionDetail = TransactionDetail.builder()
            .transactionId("TXN-20241220-123456")
            .status("SUCCESS")
            .sourceUPI("alice@okaxis")
            .destinationUPI("bob@paytm")
            .amount(new BigDecimal("500.00"))
            .fee(BigDecimal.ZERO)
            .totalDebited(new BigDecimal("500.00"))
            .timestamp(LocalDateTime.now())
            .build();
        
        when(transferService.getTransactionStatus(anyString()))
            .thenReturn(transactionDetail);
        
        // Mock transaction history
        TransactionHistoryResponse historyResponse = TransactionHistoryResponse.builder()
            .content(Collections.singletonList(transactionDetail))
            .page(PageInfo.builder()
                .number(0)
                .size(10)
                .totalElements(25L)
                .totalPages(3)
                .build())
            .build();
        
        when(transferService.getTransactionHistory(any(), any()))
            .thenReturn(historyResponse);
    }
}
```

---

## Running Contract Tests

### Generate Tests from Contracts

```bash
# Maven
mvn clean spring-cloud-contract:generateTests

# This generates test classes in target/generated-test-sources/contracts/
```

### Run Generated Tests

```bash
mvn test

# Output:
# [INFO] Running com.npci.transfer.contract.ContractVerifierTest
# [INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
```

### Generated Test Example

Spring Cloud Contract auto-generates:

`target/generated-test-sources/contracts/.../ShouldInitiateTransferSuccessfullyTest.java`

```java
@Test
public void validate_shouldInitiateTransferSuccessfully() throws Exception {
    // given:
    MockMvcRequestSpecification request = given()
        .header("Content-Type", "application/json")
        .header("Authorization", "Bearer valid-token-12345")
        .body("{ \"sourceUPI\": \"alice@okaxis\", ... }");

    // when:
    ResponseOptions response = given().spec(request)
        .post("/v1/transfers");

    // then:
    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.header("Content-Type"))
        .matches("application/json.*");
    
    // and:
    DocumentContext parsedJson = JsonPath.parse(response.getBody().asString());
    assertThatJson(parsedJson).field("['transactionId']")
        .matches("TXN-\\d{8}-\\d{6}");
    assertThatJson(parsedJson).field("['status']").isEqualTo("SUCCESS");
    // ... more assertions
}
```

---

## Publishing Contract Stubs

### 1. Generate Stubs

```bash
mvn clean install spring-cloud-contract:generateStubs
```

Stubs are generated in: `target/stubs/META-INF/com.npci/transfer-service/1.0.0/mappings/`

### 2. Publish to Maven Repository

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-contract-maven-plugin</artifactId>
            <configuration>
                <contractsRepositoryUrl>
                    https://repo.example.com/contracts
                </contractsRepositoryUrl>
            </configuration>
        </plugin>
    </plugins>
</build>
```

```bash
mvn deploy
```

---

## Consumer-Side Contract Testing

### Consumer Project Setup

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-contract-stub-runner</artifactId>
    <scope>test</scope>
</dependency>
```

### Consumer Test Example

```java
@SpringBootTest
@AutoConfigureStubRunner(
    ids = "com.npci:transfer-service:+:stubs:8080",
    stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
class TransferClientTest {

    @Autowired
    private TransferClient transferClient;

    @Test
    void shouldInitiateTransferSuccessfully() {
        // Given
        TransferRequest request = new TransferRequest(
            "alice@okaxis",
            "bob@paytm",
            new BigDecimal("500.00"),
            "Test payment"
        );

        // When
        TransferResponse response = transferClient.initiateTransfer(request);

        // Then
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        assertThat(response.getTransactionId()).matches("TXN-\\d{8}-\\d{6}");
        assertThat(response.getAmount()).isEqualTo(new BigDecimal("500.00"));
    }

    @Test
    void shouldHandleInsufficientBalanceError() {
        // Given
        TransferRequest request = new TransferRequest(
            "poor@okaxis",
            "bob@paytm",
            new BigDecimal("10000.00"),
            "Large payment"
        );

        // When/Then
        assertThatThrownBy(() -> transferClient.initiateTransfer(request))
            .isInstanceOf(InsufficientBalanceException.class)
            .hasMessageContaining("Insufficient balance");
    }
}
```

---

## Contract Testing Workflow

```
1. Define Contract (Groovy DSL)
   ↓
2. Provider runs contract tests
   ↓
3. Tests pass → Generate stubs
   ↓
4. Publish stubs to repository
   ↓
5. Consumer downloads stubs
   ↓
6. Consumer tests against stubs
   ↓
7. Both teams deploy independently
```

---

## Benefits

✅ **Catch Breaking Changes Early**: Before deployment  
✅ **Independent Development**: Teams work in parallel  
✅ **Contract as Documentation**: Single source of truth  
✅ **Fast Feedback**: No need for integration environment  
✅ **Confidence**: Both sides know contract is honored  

---

## Next Steps

1. ✅ Define contracts in Groovy DSL
2. ✅ Create BaseContractTest with mocks
3. ✅ Run `mvn spring-cloud-contract:generateTests`
4. ✅ Run `mvn test` to verify contracts
5. ✅ Publish stubs for consumers
6. ⏳ Integrate with CI pipeline (Level 22)
