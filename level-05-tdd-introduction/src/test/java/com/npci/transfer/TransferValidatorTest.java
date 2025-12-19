package com.npci.transfer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * TDD Example: TransferValidator Tests
 * 
 * These tests were written BEFORE the implementation code.
 * Each test follows the Red-Green-Refactor cycle.
 */
@DisplayName("Transfer Validator Tests")
class TransferValidatorTest {
    
    private TransferValidator validator;
    
    @BeforeEach
    void setUp() {
        validator = new TransferValidator();
    }
    
    // ====================
    // Amount Validation Tests
    // ====================
    
    @Test
    @DisplayName("Should reject null amount")
    void shouldRejectNullAmount() {
        // Act & Assert
        assertThatThrownBy(() -> validator.validateAmount(null))
            .isInstanceOf(InvalidAmountException.class)
            .hasMessage("Amount is required");
    }
    
    @Test
    @DisplayName("Should reject amount below minimum (₹1)")
    void shouldRejectAmountBelowMinimum() {
        // Arrange
        BigDecimal amount = new BigDecimal("0.50");
        
        // Act & Assert
        assertThatThrownBy(() -> validator.validateAmount(amount))
            .isInstanceOf(InvalidAmountException.class)
            .hasMessageContaining("Minimum transfer amount");
    }
    
    @Test
    @DisplayName("Should reject amount exceeding maximum (₹1,00,000)")
    void shouldRejectAmountExceedingMaximum() {
        // Arrange
        BigDecimal amount = new BigDecimal("150000");
        
        // Act & Assert
        assertThatThrownBy(() -> validator.validateAmount(amount))
            .isInstanceOf(InvalidAmountException.class)
            .hasMessageContaining("Maximum per-transaction limit");
    }
    
    @Test
    @DisplayName("Should accept minimum amount (₹1)")
    void shouldAcceptMinimumAmount() {
        // Arrange
        BigDecimal amount = new BigDecimal("1");
        
        // Act & Assert
        assertThatCode(() -> validator.validateAmount(amount))
            .doesNotThrowAnyException();
    }
    
    @Test
    @DisplayName("Should accept maximum amount (₹1,00,000)")
    void shouldAcceptMaximumAmount() {
        // Arrange
        BigDecimal amount = new BigDecimal("100000");
        
        // Act & Assert
        assertThatCode(() -> validator.validateAmount(amount))
            .doesNotThrowAnyException();
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"1.01", "500", "50000", "99999.99"})
    @DisplayName("Should accept valid amounts within range")
    void shouldAcceptValidAmounts(String amountStr) {
        // Arrange
        BigDecimal amount = new BigDecimal(amountStr);
        
        // Act & Assert
        assertThatCode(() -> validator.validateAmount(amount))
            .doesNotThrowAnyException();
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"-100", "0", "0.99", "100001", "200000"})
    @DisplayName("Should reject invalid amounts outside range")
    void shouldRejectInvalidAmounts(String amountStr) {
        // Arrange
        BigDecimal amount = new BigDecimal(amountStr);
        
        // Act & Assert
        assertThatThrownBy(() -> validator.validateAmount(amount))
            .isInstanceOf(InvalidAmountException.class);
    }
    
    // ====================
    // UPI Validation Tests
    // ====================
    
    @Test
    @DisplayName("Should reject null source UPI")
    void shouldRejectNullSourceUpi() {
        // Act & Assert
        assertThatThrownBy(() -> validator.validateUpiIds(null, "bob@paytm"))
            .isInstanceOf(InvalidUpiException.class)
            .hasMessage("Source UPI ID is required");
    }
    
    @Test
    @DisplayName("Should reject null destination UPI")
    void shouldRejectNullDestinationUpi() {
        // Act & Assert
        assertThatThrownBy(() -> validator.validateUpiIds("alice@okaxis", null))
            .isInstanceOf(InvalidUpiException.class)
            .hasMessage("Destination UPI ID is required");
    }
    
    @Test
    @DisplayName("Should reject empty source UPI")
    void shouldRejectEmptySourceUpi() {
        // Act & Assert
        assertThatThrownBy(() -> validator.validateUpiIds("", "bob@paytm"))
            .isInstanceOf(InvalidUpiException.class)
            .hasMessage("Source UPI ID is required");
    }
    
    @Test
    @DisplayName("Should reject empty destination UPI")
    void shouldRejectEmptyDestinationUpi() {
        // Act & Assert
        assertThatThrownBy(() -> validator.validateUpiIds("alice@okaxis", ""))
            .isInstanceOf(InvalidUpiException.class)
            .hasMessage("Destination UPI ID is required");
    }
    
    @Test
    @DisplayName("Should reject whitespace-only source UPI")
    void shouldRejectWhitespaceSourceUpi() {
        // Act & Assert
        assertThatThrownBy(() -> validator.validateUpiIds("   ", "bob@paytm"))
            .isInstanceOf(InvalidUpiException.class)
            .hasMessage("Source UPI ID is required");
    }
    
    @Test
    @DisplayName("Should reject same source and destination UPI")
    void shouldRejectSameSourceAndDestination() {
        // Act & Assert
        assertThatThrownBy(() -> validator.validateUpiIds("alice@okaxis", "alice@okaxis"))
            .isInstanceOf(InvalidUpiException.class)
            .hasMessage("Cannot transfer to the same account");
    }
    
    @Test
    @DisplayName("Should accept different valid UPI IDs")
    void shouldAcceptDifferentValidUpiIds() {
        // Act & Assert
        assertThatCode(() -> validator.validateUpiIds("alice@okaxis", "bob@paytm"))
            .doesNotThrowAnyException();
    }
    
    // ====================
    // Complete Validation Tests
    // ====================
    
    @Test
    @DisplayName("Should validate complete transfer request successfully")
    void shouldValidateCompleteTransferRequest() {
        // Arrange
        String sourceUPI = "alice@okaxis";
        String destinationUPI = "bob@paytm";
        BigDecimal amount = new BigDecimal("500");
        
        // Act & Assert
        assertThatCode(() -> validator.validate(sourceUPI, destinationUPI, amount))
            .doesNotThrowAnyException();
    }
    
    @Test
    @DisplayName("Should reject invalid complete transfer request")
    void shouldRejectInvalidCompleteTransferRequest() {
        // Arrange
        String sourceUPI = "alice@okaxis";
        String destinationUPI = "alice@okaxis"; // Same as source!
        BigDecimal amount = new BigDecimal("500");
        
        // Act & Assert
        assertThatThrownBy(() -> validator.validate(sourceUPI, destinationUPI, amount))
            .isInstanceOf(InvalidUpiException.class);
    }
}
