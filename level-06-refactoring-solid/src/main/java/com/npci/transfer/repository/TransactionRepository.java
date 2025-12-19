package com.npci.transfer.repository;

import com.npci.transfer.entity.Transaction;

import java.util.Optional;

/**
 * Transaction Repository Interface
 * 
 * Follows Interface Segregation Principle (I in SOLID)
 * - Focused interface for transaction operations
 */
public interface TransactionRepository {
    
    /**
     * Finds a transaction by transaction ID.
     * 
     * @param transactionId Transaction ID to search for
     * @return Optional containing transaction if found
     */
    Optional<Transaction> findByTransactionId(String transactionId);
    
    /**
     * Saves a transaction.
     * 
     * @param transaction Transaction to save
     * @return Saved transaction
     */
    Transaction save(Transaction transaction);
}
