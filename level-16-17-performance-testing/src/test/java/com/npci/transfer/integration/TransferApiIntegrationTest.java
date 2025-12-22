package com.npci.transfer.integration;

import com.npci.transfer.entity.Account;
import com.npci.transfer.repository.AccountRepository;
import com.npci.transfer.repository.TransactionRepository;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Transfer API Integration Tests
 * 
 * Tests the complete API flow: HTTP Request -> Controller -> Service -> Repository -> Database
 */
@DisplayName("Transfer API Integration Tests")
class TransferApiIntegrationTest extends BaseIntegrationTest {
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @BeforeEach
    void setUp() {
        // Spring Data JPA's deleteAll() is automatically transactional
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        
        // Create test accounts
        Account sourceAccount = Account.builder()
                .upiId("alice@okaxis")
                .phone("9876543210")
                .balance(new BigDecimal("10000.00"))
                .dailyLimit(new BigDecimal("100000.00"))
                .dailyUsed(BigDecimal.ZERO)
                .monthlyLimit(new BigDecimal("1000000.00"))
                .monthlyUsed(BigDecimal.ZERO)
                .status("ACTIVE")
                .build();
        accountRepository.save(sourceAccount);
        
        Account destinationAccount = Account.builder()
                .upiId("bob@paytm")
                .phone("9999999999")
                .balance(new BigDecimal("5000.00"))
                .dailyLimit(new BigDecimal("100000.00"))
                .dailyUsed(BigDecimal.ZERO)
                .monthlyLimit(new BigDecimal("1000000.00"))
                .monthlyUsed(BigDecimal.ZERO)
                .status("ACTIVE")
                .build();
        accountRepository.save(destinationAccount);
    }
    
    @Test
    @DisplayName("Should successfully initiate transfer")
    void shouldSuccessfullyInitiateTransfer() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "sourceUPI": "alice@okaxis",
                    "destinationUPI": "bob@paytm",
                    "amount": 500.00,
                    "remarks": "Test transfer"
                }
                """)
        .when()
            .post("/transfers")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("transactionId", notNullValue())
            .body("status", equalTo("SUCCESS"))
            .body("amount", equalTo(500.0f))
            .body("sourceUPI", equalTo("alice@okaxis"))
            .body("destinationUPI", equalTo("bob@paytm"));
    }
    
    @Test
    @DisplayName("Should return 400 for insufficient balance")
    void shouldReturnBadRequestForInsufficientBalance() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "sourceUPI": "alice@okaxis",
                    "destinationUPI": "bob@paytm",
                    "amount": 15000.00
                }
                """)
        .when()
            .post("/transfers")
        .then()
            .statusCode(400)
            .body("error", containsStringIgnoringCase("insufficient"));
    }
    
    @Test
    @DisplayName("Should return 404 for non-existent source account")
    void shouldReturnNotFoundForNonExistentSource() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "sourceUPI": "nonexistent@fake",
                    "destinationUPI": "bob@paytm",
                    "amount": 100.00
                }
                """)
        .when()
            .post("/transfers")
        .then()
            .statusCode(404)
            .body("error", containsStringIgnoringCase("not found"));
    }
    
    @Test
    @DisplayName("Should return 404 for non-existent destination account")
    void shouldReturnNotFoundForNonExistentDestination() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "sourceUPI": "alice@okaxis",
                    "destinationUPI": "fake@nonexistent",
                    "amount": 100.00
                }
                """)
        .when()
            .post("/transfers")
        .then()
            .statusCode(404)
            .body("error", containsStringIgnoringCase("not found"));
    }
    
    @Test
    @DisplayName("Should return 400 for zero amount")
    void shouldReturnBadRequestForZeroAmount() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "sourceUPI": "alice@okaxis",
                    "destinationUPI": "bob@paytm",
                    "amount": 0.00
                }
                """)
        .when()
            .post("/transfers")
        .then()
            .statusCode(400)
            .body("errors", hasItem(containsStringIgnoringCase("amount")));
    }
    
    @Test
    @DisplayName("Should return 400 for negative amount")
    void shouldReturnBadRequestForNegativeAmount() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "sourceUPI": "alice@okaxis",
                    "destinationUPI": "bob@paytm",
                    "amount": -100.00
                }
                """)
        .when()
            .post("/transfers")
        .then()
            .statusCode(400);
    }
    
    @Test
    @DisplayName("Should return 400 for invalid UPI format")
    void shouldReturnBadRequestForInvalidUpiFormat() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "sourceUPI": "invalid-upi",
                    "destinationUPI": "bob@paytm",
                    "amount": 100.00
                }
                """)
        .when()
            .post("/transfers")
        .then()
            .statusCode(400)
            .body("errors", hasItem(containsStringIgnoringCase("UPI")));
    }
    
    @Test
    @DisplayName("Should return 400 for missing required fields")
    void shouldReturnBadRequestForMissingFields() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "amount": 100.00
                }
                """)
        .when()
            .post("/transfers")
        .then()
            .statusCode(400);
    }
    
    @Test
    @DisplayName("Should return 400 for amount exceeding daily limit")
    void shouldReturnBadRequestForExcessiveAmount() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "sourceUPI": "alice@okaxis",
                    "destinationUPI": "bob@paytm",
                    "amount": 150000.00
                }
                """)
        .when()
            .post("/transfers")
        .then()
            .statusCode(400)
            .body("errors", hasItem(containsStringIgnoringCase("limit")));
    }
    
    @Test
    @DisplayName("Should process multiple sequential transfers")
    void shouldProcessMultipleTransfers() {
        // First transfer
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "sourceUPI": "alice@okaxis",
                    "destinationUPI": "bob@paytm",
                    "amount": 100.00
                }
                """)
        .when()
            .post("/transfers")
        .then()
            .statusCode(200)
            .body("status", equalTo("SUCCESS"));
        
        // Second transfer
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "sourceUPI": "alice@okaxis",
                    "destinationUPI": "bob@paytm",
                    "amount": 200.00
                }
                """)
        .when()
            .post("/transfers")
        .then()
            .statusCode(200)
            .body("status", equalTo("SUCCESS"));
    }
}
