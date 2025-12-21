package com.npci.transfer.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Fee Calculator - Calculates transaction fees
 * 
 * Follows Open/Closed Principle (O in SOLID)
 * - Open for extension (can add new strategies)
 * - Closed for modification (existing logic unchanged)
 * 
 * Currently uses simple fee strategy:
 * - Amount > ₹1,000: ₹5 fee
 * - Amount ≤ ₹1,000: No fee
 * 
 * Future: Can be extended with Strategy pattern for:
 * - Premium accounts (no fee)
 * - Weekend transfers (higher fee)
 * - International transfers (different fee)
 */
@Component
public class FeeCalculator {
    
    private static final BigDecimal FEE_THRESHOLD = new BigDecimal("1000");
    private static final BigDecimal TRANSACTION_FEE = new BigDecimal("5.00");
    
    /**
     * Calculates fee for a transfer.
     * 
     * @param amount Transfer amount
     * @return Fee to be charged
     */
    public BigDecimal calculateFee(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        
        return amount.compareTo(FEE_THRESHOLD) > 0 
            ? TRANSACTION_FEE 
            : BigDecimal.ZERO;
    }
}
