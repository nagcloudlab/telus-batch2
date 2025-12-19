package com.npci.transfer.repository;

import com.npci.transfer.entity.Account;

import java.util.Optional;

/**
 * Account Repository Interface
 * 
 * Follows Dependency Inversion Principle (D in SOLID)
 * - High-level TransferService depends on this abstraction
 * - Low-level JpaAccountRepository implements this abstraction
 * 
 * Follows Interface Segregation Principle (I in SOLID)
 * - Contains only methods needed by clients
 * - Focused, minimal interface
 * 
 * Benefits:
 * - Testable (can use in-memory implementation)
 * - Technology independent (can switch from JPA to MongoDB)
 * - Clear contract
 */
public interface AccountRepository {
    
    /**
     * Finds an account by UPI ID.
     * 
     * @param upiId UPI ID to search for
     * @return Optional containing account if found
     */
    Optional<Account> findByUpiId(String upiId);
    
    /**
     * Saves an account.
     * 
     * @param account Account to save
     * @return Saved account
     */
    Account save(Account account);
    
    /**
     * Deletes an account.
     * 
     * @param account Account to delete
     */
    void delete(Account account);
}
