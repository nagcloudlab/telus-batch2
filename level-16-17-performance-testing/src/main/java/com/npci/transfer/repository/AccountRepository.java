package com.npci.transfer.repository;

import com.npci.transfer.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Account Repository Interface
 * 
 * Spring Data JPA Repository - provides CRUD operations
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    
    /**
     * Find account by UPI ID.
     */
    Optional<Account> findByUpiId(String upiId);
    
    /**
     * Check if UPI ID exists.
     */
    boolean existsByUpiId(String upiId);
}
