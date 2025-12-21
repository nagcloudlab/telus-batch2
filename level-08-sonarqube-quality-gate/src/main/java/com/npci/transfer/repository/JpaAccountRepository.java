package com.npci.transfer.repository;

import com.npci.transfer.entity.Account;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA Account Repository Implementation
 * 
 * Follows Liskov Substitution Principle (L in SOLID)
 * - Can be substituted with any AccountRepository implementation
 * - Maintains the contract defined by AccountRepository interface
 * 
 * This is the low-level module that depends on the AccountRepository abstraction
 */
@Repository
public class JpaAccountRepository implements AccountRepository {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    public Optional<Account> findByUpiId(String upiId) {
        List<Account> accounts = entityManager
            .createQuery("SELECT a FROM Account a WHERE a.upiId = :upi", Account.class)
            .setParameter("upi", upiId)
            .getResultList();
        
        return accounts.isEmpty() ? Optional.empty() : Optional.of(accounts.get(0));
    }
    
    @Override
    public Account save(Account account) {
        if (account.getId() == null) {
            entityManager.persist(account);
            return account;
        } else {
            return entityManager.merge(account);
        }
    }
    
    @Override
    public void delete(Account account) {
        if (entityManager.contains(account)) {
            entityManager.remove(account);
        } else {
            entityManager.remove(entityManager.merge(account));
        }
    }
}
