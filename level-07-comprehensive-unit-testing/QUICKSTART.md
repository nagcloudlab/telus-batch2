# Level 7: Quick Start Guide

## 🚀 Achieve >90% Test Coverage in 3 Hours

This guide helps you write comprehensive unit tests for all layers.

---

## Prerequisites

```bash
# Have Level 6 refactored code
cd level-06-refactoring-solid

# Java 17+, Maven 3.8+
java --version
mvn --version
```

---

## Step 1: Controller Tests (45 minutes)

### Setup MockMvc Test

Create `src/test/java/com/npci/transfer/controller/TransferControllerTest.java`:

```java
@WebMvcTest(TransferController.class)
class TransferControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private TransferService transferService;
    
    @Test
    void shouldReturn200_WhenTransferSuccessful() throws Exception {
        // Arrange
        TransferResponse response = TransferResponse.builder()
            .status("SUCCESS")
            .amount(new BigDecimal("500"))
            .build();
        
        when(transferService.initiateTransfer(any()))
            .thenReturn(response);
        
        // Act & Assert
        mockMvc.perform(post("/v1/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "sourceUPI": "alice@okaxis",
                        "destinationUPI": "bob@paytm",
                        "amount": 500
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SUCCESS"));
    }
}
```

**Target**: 15-18 controller tests  
**Coverage**: 95%

---

## Step 2: Service Tests (60 minutes)

### Use Test Data Builders

```java
Account account = AccountBuilder.anAccount()
    .withUpiId("alice@okaxis")
    .withLowBalance()
    .build();
```

**Target**: 12-15 service tests  
**Coverage**: 93%

---

## Step 3: Check Coverage (5 minutes)

```bash
mvn clean test jacoco:report
open target/site/jacoco/index.html
```

**Expected**: >90% coverage ✅

---

**Time to Complete**: ~3 hours  
**Tests Written**: 50+  
**Coverage**: >90%  

🎉 **Comprehensive coverage achieved!**
