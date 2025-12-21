package com.npci.transfer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Fee Calculator Tests
 * 
 * Demonstrates:
 * - Parameterized tests
 * - Edge case testing
 * - Boundary value testing
 * - Clear test names
 */
@DisplayName("Fee Calculator Tests")
class FeeCalculatorTest {
    
    private FeeCalculator feeCalculator;
    
    @BeforeEach
    void setUp() {
        feeCalculator = new FeeCalculator();
    }
    
    // ========== Happy Path Tests ==========
    
    @Test
    @DisplayName("Should return zero fee for amounts up to ₹1,000")
    void shouldReturnZeroFee_ForAmountsUpToThreshold() {
        // Arrange
        BigDecimal amount = new BigDecimal("1000");
        
        // Act
        BigDecimal fee = feeCalculator.calculateFee(amount);
        
        // Assert
        assertThat(fee).isEqualTo(BigDecimal.ZERO);
    }
    
    @Test
    @DisplayName("Should return ₹5 fee for amounts over ₹1,000")
    void shouldReturn5Fee_ForAmountsOverThreshold() {
        // Arrange
        BigDecimal amount = new BigDecimal("1001");
        
        // Act
        BigDecimal fee = feeCalculator.calculateFee(amount);
        
        // Assert
        assertThat(fee).isEqualTo(new BigDecimal("5.00"));
    }
    
    // ========== Parameterized Tests - No Fee Amounts ==========
    
    @ParameterizedTest
    @ValueSource(strings = {"1", "100", "500", "999", "1000"})
    @DisplayName("Should not charge fee for amounts ≤ ₹1,000")
    void shouldNotChargeFee_ForAmountsBelowOrEqualToThreshold(String amountStr) {
        // Arrange
        BigDecimal amount = new BigDecimal(amountStr);
        
        // Act
        BigDecimal fee = feeCalculator.calculateFee(amount);
        
        // Assert
        assertThat(fee).isEqualTo(BigDecimal.ZERO);
    }
    
    // ========== Parameterized Tests - With Fee Amounts ==========
    
    @ParameterizedTest
    @ValueSource(strings = {"1001", "1500", "5000", "10000", "100000"})
    @DisplayName("Should charge ₹5 fee for amounts > ₹1,000")
    void shouldCharge5Fee_ForAmountsAboveThreshold(String amountStr) {
        // Arrange
        BigDecimal amount = new BigDecimal(amountStr);
        
        // Act
        BigDecimal fee = feeCalculator.calculateFee(amount);
        
        // Assert
        assertThat(fee).isEqualTo(new BigDecimal("5.00"));
    }
    
    // ========== Comprehensive Test with CSV ==========
    
    @ParameterizedTest
    @CsvSource({
        "1, 0.00",          // Minimum amount
        "100, 0.00",        // Below threshold
        "500, 0.00",        // Below threshold
        "999, 0.00",        // Just below threshold
        "999.99, 0.00",     // Just below threshold with decimals
        "1000, 0.00",       // Exactly at threshold
        "1000.00, 0.00",    // Exactly at threshold with decimals
        "1000.01, 5.00",    // Just above threshold
        "1001, 5.00",       // Just above threshold
        "1500, 5.00",       // Above threshold
        "5000, 5.00",       // Well above threshold
        "10000, 5.00",      // High amount
        "100000, 5.00"      // Maximum amount
    })
    @DisplayName("Should calculate fee correctly for various amounts")
    void shouldCalculateFeeCorrectly(String amountStr, String expectedFeeStr) {
        // Arrange
        BigDecimal amount = new BigDecimal(amountStr);
        BigDecimal expectedFee = new BigDecimal(expectedFeeStr);
        
        // Act
        BigDecimal actualFee = feeCalculator.calculateFee(amount);
        
        // Assert
        assertThat(actualFee)
            .as("Fee for amount %s should be %s", amountStr, expectedFeeStr)
            .isEqualByComparingTo(expectedFee);  // Use compareTo to ignore scale
    }
    
    // ========== Boundary Value Tests ==========
    
    @Test
    @DisplayName("Should not charge fee at exact threshold (₹1,000)")
    void shouldNotChargeFee_AtExactThreshold() {
        // Arrange - Exactly ₹1,000
        BigDecimal amount = new BigDecimal("1000.00");
        
        // Act
        BigDecimal fee = feeCalculator.calculateFee(amount);
        
        // Assert
        assertThat(fee).isEqualTo(BigDecimal.ZERO);
    }
    
    @Test
    @DisplayName("Should charge fee just above threshold (₹1,000.01)")
    void shouldChargeFee_JustAboveThreshold() {
        // Arrange - Just above ₹1,000
        BigDecimal amount = new BigDecimal("1000.01");
        
        // Act
        BigDecimal fee = feeCalculator.calculateFee(amount);
        
        // Assert
        assertThat(fee).isEqualTo(new BigDecimal("5.00"));
    }
    
    @Test
    @DisplayName("Should not charge fee just below threshold (₹999.99)")
    void shouldNotChargeFee_JustBelowThreshold() {
        // Arrange - Just below ₹1,000
        BigDecimal amount = new BigDecimal("999.99");
        
        // Act
        BigDecimal fee = feeCalculator.calculateFee(amount);
        
        // Assert
        assertThat(fee).isEqualTo(BigDecimal.ZERO);
    }
    
    // ========== Edge Case Tests ==========
    
    @Test
    @DisplayName("Should return zero fee for minimum amount (₹1)")
    void shouldReturnZeroFee_ForMinimumAmount() {
        // Arrange
        BigDecimal amount = new BigDecimal("1");
        
        // Act
        BigDecimal fee = feeCalculator.calculateFee(amount);
        
        // Assert
        assertThat(fee).isEqualTo(BigDecimal.ZERO);
    }
    
    @Test
    @DisplayName("Should return ₹5 fee for maximum amount (₹1,00,000)")
    void shouldReturn5Fee_ForMaximumAmount() {
        // Arrange
        BigDecimal amount = new BigDecimal("100000");
        
        // Act
        BigDecimal fee = feeCalculator.calculateFee(amount);
        
        // Assert
        assertThat(fee).isEqualTo(new BigDecimal("5.00"));
    }
    
    @Test
    @DisplayName("Should handle null amount gracefully")
    void shouldHandleNullAmount() {
        // Act
        BigDecimal fee = feeCalculator.calculateFee(null);
        
        // Assert
        assertThat(fee).isEqualTo(BigDecimal.ZERO);
    }
    
    // ========== Decimal Precision Tests ==========
    
    @Test
    @DisplayName("Should handle amounts with decimal places")
    void shouldHandleAmountsWithDecimals() {
        // Arrange
        BigDecimal amount = new BigDecimal("1234.56");
        
        // Act
        BigDecimal fee = feeCalculator.calculateFee(amount);
        
        // Assert
        assertThat(fee).isEqualTo(new BigDecimal("5.00"));
    }
    
    @Test
    @DisplayName("Should return fee with correct decimal precision")
    void shouldReturnFeeWithCorrectPrecision() {
        // Arrange
        BigDecimal amount = new BigDecimal("1500");
        
        // Act
        BigDecimal fee = feeCalculator.calculateFee(amount);
        
        // Assert
        assertThat(fee.scale()).isEqualTo(2);  // Two decimal places
        assertThat(fee.toString()).isEqualTo("5.00");
    }
    
    // ========== Multiple Invocation Tests ==========
    
    @Test
    @DisplayName("Should return same fee for same amount (idempotent)")
    void shouldReturnSameFee_ForSameAmount() {
        // Arrange
        BigDecimal amount = new BigDecimal("1500");
        
        // Act
        BigDecimal fee1 = feeCalculator.calculateFee(amount);
        BigDecimal fee2 = feeCalculator.calculateFee(amount);
        BigDecimal fee3 = feeCalculator.calculateFee(amount);
        
        // Assert
        assertThat(fee1).isEqualTo(fee2);
        assertThat(fee2).isEqualTo(fee3);
    }
    
    @Test
    @DisplayName("Should be stateless (no side effects)")
    void shouldBeStateless() {
        // Arrange
        BigDecimal amount1 = new BigDecimal("500");
        BigDecimal amount2 = new BigDecimal("1500");
        
        // Act - Multiple calls in different order
        BigDecimal fee1a = feeCalculator.calculateFee(amount1);
        BigDecimal fee2a = feeCalculator.calculateFee(amount2);
        BigDecimal fee1b = feeCalculator.calculateFee(amount1);
        BigDecimal fee2b = feeCalculator.calculateFee(amount2);
        
        // Assert - Results should be consistent
        assertThat(fee1a).isEqualTo(fee1b);
        assertThat(fee2a).isEqualTo(fee2b);
    }
    
    // ========== Performance Tests ==========
    
    @Test
    @DisplayName("Should calculate fee quickly (<1ms)")
    void shouldCalculateFeeQuickly() {
        // Arrange
        BigDecimal amount = new BigDecimal("1500");
        
        // Act & Assert
        long startTime = System.nanoTime();
        BigDecimal fee = feeCalculator.calculateFee(amount);
        long endTime = System.nanoTime();
        
        long durationMs = (endTime - startTime) / 1_000_000;
        
        assertThat(fee).isNotNull();
        assertThat(durationMs).isLessThan(1);  // Should be instant
    }
}
