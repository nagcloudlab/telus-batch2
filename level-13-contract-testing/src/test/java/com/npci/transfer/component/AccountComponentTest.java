package com.npci.transfer.component;

import com.npci.transfer.config.PostgreSQLTestContainer;
import com.npci.transfer.entity.Account;
import com.npci.transfer.repository.AccountRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Component Tests for Account Repository with PostgreSQL
 * 
 * These tests demonstrate the value of Testcontainers:
 * - Real database constraints (H2 might not enforce these)
 * - PostgreSQL-specific behaviors
 * - Transaction management
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Account Component Tests - PostgreSQL")
class AccountComponentTest extends PostgreSQLTestContainer {
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Test
    @DisplayName("Should save account with valid UPI ID")
    void shouldSaveAccountWithValidUpiId() {
        // Given
        Account account = Account.builder()
                .upiId("test@okaxis")
                .phone("9876543210")
                .balance(new BigDecimal("1000.00"))
                .dailyLimit(new BigDecimal("100000.00"))
                .dailyUsed(BigDecimal.ZERO)
                .monthlyLimit(new BigDecimal("1000000.00"))
                .monthlyUsed(BigDecimal.ZERO)
                .status("ACTIVE")
                .build();
        
        // When
        Account saved = accountRepository.save(account);
        
        // Then
        assertNotNull(saved.getId());
        assertEquals("test@okaxis", saved.getUpiId());
        assertEquals(new BigDecimal("1000.00"), saved.getBalance());
    }
    
    @Test
    @DisplayName("Should enforce unique constraint on UPI ID (PostgreSQL)")
    void shouldEnforceUniqueConstraintOnUpiId() {
        // Given - First account
        Account account1 = Account.builder()
                .upiId("duplicate@okaxis")
                .phone("9876543210")
                .balance(new BigDecimal("1000.00"))
                .dailyLimit(new BigDecimal("100000.00"))
                .dailyUsed(BigDecimal.ZERO)
                .monthlyLimit(new BigDecimal("1000000.00"))
                .monthlyUsed(BigDecimal.ZERO)
                .status("ACTIVE")
                .build();
        accountRepository.save(account1);
        
        // Given - Second account with same UPI ID
        Account account2 = Account.builder()
                .upiId("duplicate@okaxis")  // Duplicate!
                .phone("9999999999")
                .balance(new BigDecimal("2000.00"))
                .dailyLimit(new BigDecimal("100000.00"))
                .dailyUsed(BigDecimal.ZERO)
                .monthlyLimit(new BigDecimal("1000000.00"))
                .monthlyUsed(BigDecimal.ZERO)
                .status("ACTIVE")
                .build();
        
        // Then - PostgreSQL enforces unique constraint
        Exception exception = assertThrows(Exception.class, () -> {
            accountRepository.save(account2);
            accountRepository.flush();  // Force immediate execution
        });
        
        // Verify it's a constraint violation
        assertTrue(
            exception instanceof DataIntegrityViolationException || 
            exception instanceof ConstraintViolationException,
            "Expected constraint violation exception but got: " + exception.getClass()
        );
    }
    
    @Test
    @DisplayName("Should handle decimal precision correctly (PostgreSQL)")
    void shouldHandleDecimalPrecisionCorrectly() {
        // Given
        Account account = Account.builder()
                .upiId("precision@okaxis")
                .phone("9876543210")
                .balance(new BigDecimal("1234.56"))  // Exact decimal
                .dailyLimit(new BigDecimal("100000.00"))
                .dailyUsed(BigDecimal.ZERO)
                .monthlyLimit(new BigDecimal("1000000.00"))
                .monthlyUsed(BigDecimal.ZERO)
                .status("ACTIVE")
                .build();
        
        // When
        Account saved = accountRepository.save(account);
        Account retrieved = accountRepository.findById(saved.getId()).orElseThrow();
        
        // Then - PostgreSQL maintains exact precision
        assertEquals(new BigDecimal("1234.56"), retrieved.getBalance());
        assertEquals(0, new BigDecimal("1234.56").compareTo(retrieved.getBalance()));
    }
    
    @Test
    @DisplayName("Should find account by UPI ID")
    void shouldFindAccountByUpiId() {
        // Given
        Account account = Account.builder()
                .upiId("findme@okaxis")
                .phone("9876543210")
                .balance(new BigDecimal("1000.00"))
                .dailyLimit(new BigDecimal("100000.00"))
                .dailyUsed(BigDecimal.ZERO)
                .monthlyLimit(new BigDecimal("1000000.00"))
                .monthlyUsed(BigDecimal.ZERO)
                .status("ACTIVE")
                .build();
        accountRepository.save(account);
        
        // When
        Account found = accountRepository.findByUpiId("findme@okaxis").orElse(null);
        
        // Then
        assertNotNull(found);
        assertEquals("findme@okaxis", found.getUpiId());
    }
    
    @Test
    @DisplayName("Should handle zero balance")
    void shouldHandleZeroBalance() {
        // Given
        Account account = Account.builder()
                .upiId("zero@okaxis")
                .phone("9876543210")
                .balance(BigDecimal.ZERO)
                .dailyLimit(new BigDecimal("100000.00"))
                .dailyUsed(BigDecimal.ZERO)
                .monthlyLimit(new BigDecimal("1000000.00"))
                .monthlyUsed(BigDecimal.ZERO)
                .status("ACTIVE")
                .build();
        
        // When
        Account saved = accountRepository.save(account);
        
        // Then
        assertEquals(BigDecimal.ZERO, saved.getBalance());
        assertEquals(0, BigDecimal.ZERO.compareTo(saved.getBalance()));
    }
    
    @Test
    @DisplayName("Should update balance correctly")
    void shouldUpdateBalanceCorrectly() {
        // Given
        Account account = Account.builder()
                .upiId("update@okaxis")
                .phone("9876543210")
                .balance(new BigDecimal("1000.00"))
                .dailyLimit(new BigDecimal("100000.00"))
                .dailyUsed(BigDecimal.ZERO)
                .monthlyLimit(new BigDecimal("1000000.00"))
                .monthlyUsed(BigDecimal.ZERO)
                .status("ACTIVE")
                .build();
        Account saved = accountRepository.save(account);
        
        // When
        saved.setBalance(new BigDecimal("1500.50"));
        Account updated = accountRepository.save(saved);
        
        // Then
        assertEquals(new BigDecimal("1500.50"), updated.getBalance());
    }
}
