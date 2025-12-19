package com.npci.transfer;

/**
 * Exception thrown when UPI ID is invalid.
 * 
 * This includes:
 * - Null UPI ID
 * - Empty UPI ID
 * - Whitespace-only UPI ID
 * - Same source and destination UPI
 * - Invalid UPI format
 */
public class InvalidUpiException extends RuntimeException {
    
    /**
     * Creates a new InvalidUpiException with the specified message.
     * 
     * @param message Error message explaining what's wrong
     */
    public InvalidUpiException(String message) {
        super(message);
    }
}
