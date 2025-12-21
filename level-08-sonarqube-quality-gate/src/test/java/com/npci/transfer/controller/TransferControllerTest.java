package com.npci.transfer.controller;

import com.npci.transfer.dto.TransferRequest;
import com.npci.transfer.dto.TransferResponse;
import com.npci.transfer.exception.*;
import com.npci.transfer.service.TransferService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Controller Layer Tests
 * 
 * Tests HTTP layer only - business logic is mocked
 * 
 * What we test:
 * - HTTP status codes
 * - Request/response mapping
 * - Validation
 * - Error handling
 * 
 * What we DON'T test:
 * - Business logic (tested in ServiceTest)
 * - Database access (tested in RepositoryTest)
 */
@WebMvcTest(TransferController.class)
@DisplayName("Transfer Controller Tests")
class TransferControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private TransferService transferService;
    
    // ========== Happy Path Tests ==========
    
    @Test
    @DisplayName("Should return 200 OK when transfer is successful")
    void shouldReturn200_WhenTransferSuccessful() throws Exception {
        // Arrange
        TransferResponse response = TransferResponse.builder()
            .transactionId("TXN-20241220-123456")
            .status("SUCCESS")
            .sourceUPI("alice@okaxis")
            .destinationUPI("bob@paytm")
            .amount(new BigDecimal("500"))
            .fee(BigDecimal.ZERO)
            .totalDebited(new BigDecimal("500"))
            .timestamp(LocalDateTime.now())
            .remarks("Test transfer")
            .build();
        
        when(transferService.initiateTransfer(any(TransferRequest.class)))
            .thenReturn(response);
        
        // Act & Assert
        mockMvc.perform(post("/v1/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "sourceUPI": "alice@okaxis",
                        "destinationUPI": "bob@paytm",
                        "amount": 500,
                        "remarks": "Test transfer"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.transactionId").value("TXN-20241220-123456"))
            .andExpect(jsonPath("$.status").value("SUCCESS"))
            .andExpect(jsonPath("$.sourceUPI").value("alice@okaxis"))
            .andExpect(jsonPath("$.destinationUPI").value("bob@paytm"))
            .andExpect(jsonPath("$.amount").value(500))
            .andExpect(jsonPath("$.fee").value(0))
            .andExpect(jsonPath("$.totalDebited").value(500));
        
        verify(transferService, times(1)).initiateTransfer(any());
    }
    
    @Test
    @DisplayName("Should include fee in response when applicable")
    void shouldIncludeFeeInResponse() throws Exception {
        // Arrange
        TransferResponse response = TransferResponse.builder()
            .transactionId("TXN-20241220-123457")
            .status("SUCCESS")
            .sourceUPI("alice@okaxis")
            .destinationUPI("bob@paytm")
            .amount(new BigDecimal("1500"))
            .fee(new BigDecimal("5.00"))
            .totalDebited(new BigDecimal("1505.00"))
            .timestamp(LocalDateTime.now())
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
                        "amount": 1500
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.amount").value(1500))
            .andExpect(jsonPath("$.fee").value(5.00))
            .andExpect(jsonPath("$.totalDebited").value(1505.00));
    }
    
    // ========== Validation Tests ==========
    
    @Test
    @DisplayName("Should return 400 Bad Request when source UPI is missing")
    void shouldReturn400_WhenSourceUPIMissing() throws Exception {
        mockMvc.perform(post("/v1/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "destinationUPI": "bob@paytm",
                        "amount": 500
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Validation Error"))
            .andExpect(jsonPath("$.validationErrors.sourceUPI").exists());
    }
    
    @Test
    @DisplayName("Should return 400 Bad Request when destination UPI is missing")
    void shouldReturn400_WhenDestinationUPIMissing() throws Exception {
        mockMvc.perform(post("/v1/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "sourceUPI": "alice@okaxis",
                        "amount": 500
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.validationErrors.destinationUPI").exists());
    }
    
    @Test
    @DisplayName("Should return 400 Bad Request when amount is missing")
    void shouldReturn400_WhenAmountMissing() throws Exception {
        mockMvc.perform(post("/v1/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "sourceUPI": "alice@okaxis",
                        "destinationUPI": "bob@paytm"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.validationErrors.amount").exists());
    }
    
    @Test
    @DisplayName("Should return 400 Bad Request when amount is negative")
    void shouldReturn400_WhenAmountNegative() throws Exception {
        mockMvc.perform(post("/v1/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "sourceUPI": "alice@okaxis",
                        "destinationUPI": "bob@paytm",
                        "amount": -100
                    }
                    """))
            .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should return 400 Bad Request when amount is zero")
    void shouldReturn400_WhenAmountZero() throws Exception {
        mockMvc.perform(post("/v1/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "sourceUPI": "alice@okaxis",
                        "destinationUPI": "bob@paytm",
                        "amount": 0
                    }
                    """))
            .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should return 400 Bad Request when amount exceeds maximum")
    void shouldReturn400_WhenAmountExceedsMaximum() throws Exception {
        mockMvc.perform(post("/v1/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "sourceUPI": "alice@okaxis",
                        "destinationUPI": "bob@paytm",
                        "amount": 200000
                    }
                    """))
            .andExpect(status().isBadRequest());
    }
    
    @ParameterizedTest
    @CsvSource({
        "alice okaxis, Invalid UPI format",
        "alice@, Invalid UPI format",
        "@okaxis, Invalid UPI format"
    })
    @DisplayName("Should return 400 Bad Request when UPI format is invalid")
    void shouldReturn400_WhenUPIFormatInvalid(String invalidUPI, String expectedError) throws Exception {
        mockMvc.perform(post("/v1/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                    {
                        "sourceUPI": "%s",
                        "destinationUPI": "bob@paytm",
                        "amount": 500
                    }
                    """, invalidUPI)))
            .andExpect(status().isBadRequest());
    }
    
    // ========== Exception Handling Tests ==========
    
    @Test
    @DisplayName("Should return 404 Not Found when account not found")
    void shouldReturn404_WhenAccountNotFound() throws Exception {
        // Arrange
        when(transferService.initiateTransfer(any()))
            .thenThrow(new AccountNotFoundException("Account not found"));
        
        // Act & Assert
        mockMvc.perform(post("/v1/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "sourceUPI": "nonexistent@okaxis",
                        "destinationUPI": "bob@paytm",
                        "amount": 500
                    }
                    """))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Account Not Found"))
            .andExpect(jsonPath("$.message").value("Account not found"))
            .andExpect(jsonPath("$.status").value(404));
    }
    
    @Test
    @DisplayName("Should return 400 Bad Request when insufficient balance")
    void shouldReturn400_WhenInsufficientBalance() throws Exception {
        // Arrange
        when(transferService.initiateTransfer(any()))
            .thenThrow(new InsufficientBalanceException("Insufficient balance"));
        
        // Act & Assert
        mockMvc.perform(post("/v1/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "sourceUPI": "poor@okaxis",
                        "destinationUPI": "bob@paytm",
                        "amount": 10000
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Insufficient Balance"))
            .andExpect(jsonPath("$.message").value("Insufficient balance"));
    }
    
    @Test
    @DisplayName("Should return 400 Bad Request when transfer is invalid")
    void shouldReturn400_WhenTransferInvalid() throws Exception {
        // Arrange
        when(transferService.initiateTransfer(any()))
            .thenThrow(new InvalidTransferException("Cannot transfer to same account"));
        
        // Act & Assert
        mockMvc.perform(post("/v1/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "sourceUPI": "alice@okaxis",
                        "destinationUPI": "alice@okaxis",
                        "amount": 500
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Invalid Transfer"))
            .andExpect(jsonPath("$.message").value("Cannot transfer to same account"));
    }
    
    @Test
    @DisplayName("Should return 500 Internal Server Error when unexpected error occurs")
    void shouldReturn500_WhenUnexpectedErrorOccurs() throws Exception {
        // Arrange
        when(transferService.initiateTransfer(any()))
            .thenThrow(new RuntimeException("Unexpected error"));
        
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
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.error").value("Internal Server Error"))
            .andExpect(jsonPath("$.status").value(500));
    }
    
    // ========== Health Endpoint Tests ==========
    
    @Test
    @DisplayName("Should return 200 OK for health check")
    void shouldReturn200_ForHealthCheck() throws Exception {
        mockMvc.perform(get("/v1/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"))
            .andExpect(jsonPath("$.timestamp").exists());
    }
    
    // ========== Content Type Tests ==========
    
    @Test
    @DisplayName("Should return 415 Unsupported Media Type when content type is not JSON")
    void shouldReturn415_WhenContentTypeNotJSON() throws Exception {
        mockMvc.perform(post("/v1/transfers")
                .contentType(MediaType.TEXT_PLAIN)
                .content("plain text"))
            .andExpect(status().isUnsupportedMediaType());
    }
    
    // ========== Remarks Field Tests ==========
    
    @Test
    @DisplayName("Should accept transfer without remarks")
    void shouldAcceptTransfer_WithoutRemarks() throws Exception {
        // Arrange
        TransferResponse response = TransferResponse.builder()
            .transactionId("TXN-123")
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
            .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("Should accept transfer with remarks")
    void shouldAcceptTransfer_WithRemarks() throws Exception {
        // Arrange
        TransferResponse response = TransferResponse.builder()
            .transactionId("TXN-123")
            .status("SUCCESS")
            .amount(new BigDecimal("500"))
            .remarks("Lunch payment")
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
                        "amount": 500,
                        "remarks": "Lunch payment"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.remarks").value("Lunch payment"));
    }
}
