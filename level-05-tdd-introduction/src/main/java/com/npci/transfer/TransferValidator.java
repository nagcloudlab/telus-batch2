package com.npci.transfer;

import java.math.BigDecimal;

/**
 * Validates transfer requests.
 * This class was built using Test-Driven Development (TDD).
 * 
 * Each method was implemented only after writing a failing test first.
 */
public class TransferValidator {
    
    private static final BigDecimal MIN_AMOUNT = new BigDecimal("1");
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("100000");
    
    /**
     * Validates a complete transfer request.
     * 
     * @param sourceUPI Source UPI ID
     * @param destinationUPI Destination UPI ID
     * @param amount Transfer amount
     * @throws InvalidAmountException if amount is invalid
     * @throws InvalidUpiException if UPI IDs are invalid
     */
    public void validate(String sourceUPI, String destinationUPI, BigDecimal amount) {
        validateUpiIds(sourceUPI, destinationUPI);
        validateAmount(amount);
    }
    
    /**
     * Validates transfer amount.
     * 
     * @param amount Amount to validate
     * @throws InvalidAmountException if amount is null, below minimum, or above maximum
     */
    public void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new InvalidAmountException("Amount is required");
        }
        
        validateMinimumAmount(amount);
        validateMaximumAmount(amount);
    }
    
    /**
     * Validates UPI IDs.
     * 
     * @param sourceUPI Source UPI ID
     * @param destinationUPI Destination UPI ID
     * @throws InvalidUpiException if UPI IDs are invalid
     */
    public void validateUpiIds(String sourceUPI, String destinationUPI) {
        validateRequiredUpi(sourceUPI, "Source");
        validateRequiredUpi(destinationUPI, "Destination");
        validateDifferentUpiIds(sourceUPI, destinationUPI);
    }
    
    /**
     * Validates minimum amount (₹1).
     */
    private void validateMinimumAmount(BigDecimal amount) {
        if (amount.compareTo(MIN_AMOUNT) < 0) {
            throw new InvalidAmountException(
                String.format("Minimum transfer amount is ₹%s", MIN_AMOUNT)
            );
        }
    }
    
    /**
     * Validates maximum amount (₹1,00,000).
     */
    private void validateMaximumAmount(BigDecimal amount) {
        if (amount.compareTo(MAX_AMOUNT) > 0) {
            throw new InvalidAmountException(
                String.format("Maximum per-transaction limit is ₹%s", 
                    formatAmount(MAX_AMOUNT))
            );
        }
    }
    
    /**
     * Validates that UPI ID is not null or empty.
     */
    private void validateRequiredUpi(String upiId, String fieldName) {
        if (upiId == null || upiId.trim().isEmpty()) {
            throw new InvalidUpiException(fieldName + " UPI ID is required");
        }
    }
    
    /**
     * Validates that source and destination UPI IDs are different.
     */
    private void validateDifferentUpiIds(String sourceUPI, String destinationUPI) {
        if (sourceUPI.equals(destinationUPI)) {
            throw new InvalidUpiException("Cannot transfer to the same account");
        }
    }
    
    /**
     * Formats amount for display.
     */
    private String formatAmount(BigDecimal amount) {
        return String.format("%,d", amount.longValue());
    }
}
