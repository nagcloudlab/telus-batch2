package com.npci.transfer.repository;

import com.npci.transfer.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Transaction Repository Interface
 * 
 * Spring Data JPA Repository - provides CRUD operations
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    /**
     * Find transaction by transaction ID.
     */
    Optional<Transaction> findByTransactionId(String transactionId);
}
