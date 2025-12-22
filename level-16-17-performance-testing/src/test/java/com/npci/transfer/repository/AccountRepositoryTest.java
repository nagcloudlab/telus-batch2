package com.npci.transfer.repository;

import com.npci.transfer.entity.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Account Repository Tests
 * 
 * Tests data access layer with real database (H2 in-memory)
 * 
 * @DataJpaTest provides:
 * - In-memory database
 * - Transaction rollback after each test
 * - TestEntityManager for setup
 */
@DataJpaTest
@DisplayName("Account Repository Tests")
class AccountRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private AccountRepository accountRepository;
    
    private Account testAccount;
    
    @BeforeEach
    void setUp() {
        testAccount = new Account();
        testAccount.setUpiId("alice@okaxis");
        testAccount.setPhone("9876543210");
        testAccount.setBalance(new BigDecimal("10000"));
        testAccount.setDailyLimit(new BigDecimal("50000"));
        testAccount.setDailyUsed(BigDecimal.ZERO);
        testAccount.setMonthlyLimit(new BigDecimal("200000"));
        testAccount.setMonthlyUsed(BigDecimal.ZERO);
        testAccount.setStatus("ACTIVE");
    }
    
    // ========== Find By UPI ID Tests ==========
    
    @Test
    @DisplayName("Should find account by UPI ID when it exists")
    void shouldFindAccountByUpiId_WhenItExists() {
        // Arrange
        entityManager.persist(testAccount);
        entityManager.flush();
        
        // Act
        Optional<Account> found = accountRepository.findByUpiId("alice@okaxis");
        
        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getUpiId()).isEqualTo("alice@okaxis");
        assertThat(found.get().getBalance()).isEqualByComparingTo(new BigDecimal("10000"));
        assertThat(found.get().getPhone()).isEqualTo("9876543210");
    }
    
    @Test
    @DisplayName("Should return empty when account not found")
    void shouldReturnEmpty_WhenAccountNotFound() {
        // Act
        Optional<Account> found = accountRepository.findByUpiId("nonexistent@upi");
        
        // Assert
        assertThat(found).isEmpty();
    }
    
    @Test
    @DisplayName("Should find correct account when multiple accounts exist")
    void shouldFindCorrectAccount_WhenMultipleAccountsExist() {
        // Arrange
        Account alice = createAccount("alice@okaxis", "9876543210", "10000");
        Account bob = createAccount("bob@paytm", "9876543211", "5000");
        Account charlie = createAccount("charlie@gpay", "9876543212", "15000");
        
        entityManager.persist(alice);
        entityManager.persist(bob);
        entityManager.persist(charlie);
        entityManager.flush();
        
        // Act
        Optional<Account> found = accountRepository.findByUpiId("bob@paytm");
        
        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getUpiId()).isEqualTo("bob@paytm");
        assertThat(found.get().getBalance()).isEqualByComparingTo(new BigDecimal("5000"));
    }
    
    // ========== Save Tests ==========
    
    @Test
    @DisplayName("Should save new account successfully")
    void shouldSaveNewAccount() {
        // Act
        Account saved = accountRepository.save(testAccount);
        entityManager.flush();
        
        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUpiId()).isEqualTo("alice@okaxis");
        
        // Verify it's in database
        Account found = entityManager.find(Account.class, saved.getId());
        assertThat(found).isNotNull();
        assertThat(found.getUpiId()).isEqualTo("alice@okaxis");
    }
    
    @Test
    @DisplayName("Should update existing account")
    void shouldUpdateExistingAccount() {
        // Arrange
        entityManager.persist(testAccount);
        entityManager.flush();
        
        // Act - Update balance
        testAccount.setBalance(new BigDecimal("15000"));
        Account updated = accountRepository.save(testAccount);
        entityManager.flush();
        entityManager.clear(); // Clear cache to force database read
        
        // Assert
        Account found = entityManager.find(Account.class, updated.getId());
        assertThat(found.getBalance()).isEqualByComparingTo(new BigDecimal("15000"));
    }
    
    @Test
    @DisplayName("Should persist all account fields")
    void shouldPersistAllAccountFields() {
        // Arrange
        testAccount.setDailyUsed(new BigDecimal("5000"));
        testAccount.setMonthlyUsed(new BigDecimal("20000"));
        
        // Act
        Account saved = accountRepository.save(testAccount);
        entityManager.flush();
        entityManager.clear();
        
        // Assert
        Account found = entityManager.find(Account.class, saved.getId());
        assertThat(found.getUpiId()).isEqualTo("alice@okaxis");
        assertThat(found.getPhone()).isEqualTo("9876543210");
        assertThat(found.getBalance()).isEqualByComparingTo(new BigDecimal("10000"));
        assertThat(found.getDailyLimit()).isEqualByComparingTo(new BigDecimal("50000"));
        assertThat(found.getDailyUsed()).isEqualByComparingTo(new BigDecimal("5000"));
        assertThat(found.getMonthlyLimit()).isEqualByComparingTo(new BigDecimal("200000"));
        assertThat(found.getMonthlyUsed()).isEqualByComparingTo(new BigDecimal("20000"));
        assertThat(found.getStatus()).isEqualTo("ACTIVE");
    }
    
    // ========== Delete Tests ==========
    
    @Test
    @DisplayName("Should delete account successfully")
    void shouldDeleteAccount() {
        // Arrange
        entityManager.persist(testAccount);
        entityManager.flush();
        Long accountId = testAccount.getId();
        
        // Act
        accountRepository.delete(testAccount);
        entityManager.flush();
        
        // Assert
        Account found = entityManager.find(Account.class, accountId);
        assertThat(found).isNull();
    }
    
    // ========== Edge Case Tests ==========
    
    @Test
    @DisplayName("Should handle zero balance")
    void shouldHandleZeroBalance() {
        // Arrange
        testAccount.setBalance(BigDecimal.ZERO);
        
        // Act
        Account saved = accountRepository.save(testAccount);
        entityManager.flush();
        
        // Assert
        Optional<Account> found = accountRepository.findByUpiId("alice@okaxis");
        assertThat(found).isPresent();
        assertThat(found.get().getBalance()).isEqualTo(BigDecimal.ZERO);
    }
    
    @Test
    @DisplayName("Should handle large balance values")
    void shouldHandleLargeBalanceValues() {
        // Arrange
        testAccount.setBalance(new BigDecimal("9999999999.99"));
        
        // Act
        Account saved = accountRepository.save(testAccount);
        entityManager.flush();
        
        // Assert
        Optional<Account> found = accountRepository.findByUpiId("alice@okaxis");
        assertThat(found).isPresent();
        assertThat(found.get().getBalance()).isEqualByComparingTo(new BigDecimal("9999999999.99"));
    }
    
    @Test
    @DisplayName("Should maintain decimal precision")
    void shouldMaintainDecimalPrecision() {
        // Arrange
        testAccount.setBalance(new BigDecimal("1234.56"));
        
        // Act
        Account saved = accountRepository.save(testAccount);
        entityManager.flush();
        entityManager.clear();
        
        // Assert
        Account found = entityManager.find(Account.class, saved.getId());
        assertThat(found.getBalance()).isEqualByComparingTo(new BigDecimal("1234.56"));
    }
    
    // ========== Helper Methods ==========
    
    private Account createAccount(String upiId, String phone, String balance) {
        Account account = new Account();
        account.setUpiId(upiId);
        account.setPhone(phone);
        account.setBalance(new BigDecimal(balance));
        account.setDailyLimit(new BigDecimal("50000"));
        account.setDailyUsed(BigDecimal.ZERO);
        account.setMonthlyLimit(new BigDecimal("200000"));
        account.setMonthlyUsed(BigDecimal.ZERO);
        account.setStatus("ACTIVE");
        return account;
    }
}
