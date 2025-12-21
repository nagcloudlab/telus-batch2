package com.npci.transfer.repository;

import com.npci.transfer.entity.Transaction;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA Transaction Repository Implementation
 */
@Repository
public class JpaTransactionRepository implements TransactionRepository {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    public Optional<Transaction> findByTransactionId(String transactionId) {
        List<Transaction> transactions = entityManager
            .createQuery("SELECT t FROM Transaction t WHERE t.transactionId = :txnId", 
                Transaction.class)
            .setParameter("txnId", transactionId)
            .getResultList();
        
        return transactions.isEmpty() ? Optional.empty() : Optional.of(transactions.get(0));
    }
    
    @Override
    public Transaction save(Transaction transaction) {
        if (transaction.getId() == null) {
            entityManager.persist(transaction);
            return transaction;
        } else {
            return entityManager.merge(transaction);
        }
    }
}
