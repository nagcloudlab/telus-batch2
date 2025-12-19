package com.npci.transfer;

/**
 * Exception thrown when transfer amount is invalid.
 * 
 * This includes:
 * - Null amount
 * - Amount below minimum (₹1)
 * - Amount above maximum (₹1,00,000)
 * - Negative amount
 */
public class InvalidAmountException extends RuntimeException {
    
    /**
     * Creates a new InvalidAmountException with the specified message.
     * 
     * @param message Error message explaining what's wrong
     */
    public InvalidAmountException(String message) {
        super(message);
    }
}
