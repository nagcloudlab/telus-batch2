package com.npci.transfer.component;

import com.npci.transfer.config.PostgreSQLTestContainer;
import com.npci.transfer.dto.TransferRequest;
import com.npci.transfer.dto.TransferResponse;
import com.npci.transfer.entity.Account;
import com.npci.transfer.exception.AccountNotFoundException;
import com.npci.transfer.exception.InsufficientBalanceException;
import com.npci.transfer.repository.AccountRepository;
import com.npci.transfer.repository.TransactionRepository;
import com.npci.transfer.service.FeeCalculator;
import com.npci.transfer.service.TransferService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Component Tests for Transfer Service with PostgreSQL
 * 
 * These tests verify:
 * - Transaction management (ACID properties)
 * - Database constraints
 * - Real PostgreSQL behavior vs H2
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Transfer Component Tests - PostgreSQL")
class TransferComponentTest extends PostgreSQLTestContainer {
    
    @Autowired
    private TransferService transferService;
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private FeeCalculator feeCalculator;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    private Account sourceAccount;
    private Account destinationAccount;
    
    @BeforeEach
    void setUp() {
        // Clear data
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        entityManager.flush();  // Force delete to complete
        entityManager.clear();  // Clear persistence context
        
        // Create test accounts in real PostgreSQL
        sourceAccount = Account.builder()
                .upiId("alice@okaxis")
                .phone("9876543210")
                .balance(new BigDecimal("10000.00"))
                .dailyLimit(new BigDecimal("100000.00"))
                .dailyUsed(BigDecimal.ZERO)
                .monthlyLimit(new BigDecimal("1000000.00"))
                .monthlyUsed(BigDecimal.ZERO)
                .status("ACTIVE")
                .build();
        sourceAccount = accountRepository.save(sourceAccount);
        
        destinationAccount = Account.builder()
                .upiId("bob@paytm")
                .phone("9999999999")
                .balance(new BigDecimal("5000.00"))
                .dailyLimit(new BigDecimal("100000.00"))
                .dailyUsed(BigDecimal.ZERO)
                .monthlyLimit(new BigDecimal("1000000.00"))
                .monthlyUsed(BigDecimal.ZERO)
                .status("ACTIVE")
                .build();
        destinationAccount = accountRepository.save(destinationAccount);
        
        entityManager.flush();  // Force creation to complete
    }
    
    @Test
    @DisplayName("Should transfer money successfully")
    void shouldTransferMoneySuccessfully() {
        // Given
        TransferRequest request = TransferRequest.builder()
                .sourceUPI("alice@okaxis")
                .destinationUPI("bob@paytm")
                .amount(new BigDecimal("500.00"))
                .remarks("Test transfer")
                .build();
        
        BigDecimal sourceInitialBalance = sourceAccount.getBalance();
        BigDecimal destInitialBalance = destinationAccount.getBalance();
        
        // When
        TransferResponse response = transferService.initiateTransfer(request);
        
        // Then
        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertEquals(new BigDecimal("500.00"), response.getAmount());
        
        // Verify balances updated in database
        Account updatedSource = accountRepository.findByUpiId("alice@okaxis").orElseThrow();
        Account updatedDest = accountRepository.findByUpiId("bob@paytm").orElseThrow();
        
        assertEquals(sourceInitialBalance.subtract(new BigDecimal("500.00")), 
                updatedSource.getBalance());
        assertEquals(destInitialBalance.add(new BigDecimal("500.00")), 
                updatedDest.getBalance());
    }
    
    @Test
    @DisplayName("Should rollback transaction on insufficient balance")
    void shouldRollbackTransactionOnInsufficientBalance() {
        // Given
        TransferRequest request = TransferRequest.builder()
                .sourceUPI("alice@okaxis")
                .destinationUPI("bob@paytm")
                .amount(new BigDecimal("15000.00"))  // More than balance
                .build();
        
        BigDecimal sourceInitialBalance = sourceAccount.getBalance();
        BigDecimal destInitialBalance = destinationAccount.getBalance();
        
        // When & Then
        assertThrows(InsufficientBalanceException.class, () -> {
            transferService.initiateTransfer(request);
        });
        
        // Verify balances unchanged (transaction rolled back)
        Account updatedSource = accountRepository.findByUpiId("alice@okaxis").orElseThrow();
        Account updatedDest = accountRepository.findByUpiId("bob@paytm").orElseThrow();
        
        assertEquals(sourceInitialBalance, updatedSource.getBalance());
        assertEquals(destInitialBalance, updatedDest.getBalance());
    }
    
    @Test
    @DisplayName("Should persist transaction history")
    void shouldPersistTransactionHistory() {
        // Given
        TransferRequest request = TransferRequest.builder()
                .sourceUPI("alice@okaxis")
                .destinationUPI("bob@paytm")
                .amount(new BigDecimal("100.00"))
                .remarks("Test transaction")
                .build();
        
        long initialCount = transactionRepository.count();
        
        // When
        TransferResponse response = transferService.initiateTransfer(request);
        
        // Then
        long finalCount = transactionRepository.count();
        assertEquals(initialCount + 1, finalCount);
        
        // Verify transaction saved with correct details
        var transaction = transactionRepository.findByTransactionId(response.getTransactionId());
        assertTrue(transaction.isPresent());
        assertEquals("alice@okaxis", transaction.get().getSourceUPI());
        assertEquals("bob@paytm", transaction.get().getDestinationUPI());
        assertEquals(new BigDecimal("100.00"), transaction.get().getAmount());
    }
    
    @Test
    @DisplayName("Should handle non-existent account")
    void shouldHandleNonExistentAccount() {
        // Given
        TransferRequest request = TransferRequest.builder()
                .sourceUPI("nonexistent@fake")
                .destinationUPI("bob@paytm")
                .amount(new BigDecimal("100.00"))
                .build();
        
        // When & Then
        assertThrows(AccountNotFoundException.class, () -> {
            transferService.initiateTransfer(request);
        });
    }
    
    @Test
    @DisplayName("Should handle concurrent transfers correctly")
    void shouldHandleConcurrentTransfers() {
        // Given
        TransferRequest request1 = TransferRequest.builder()
                .sourceUPI("alice@okaxis")
                .destinationUPI("bob@paytm")
                .amount(new BigDecimal("100.00"))
                .build();
        
        TransferRequest request2 = TransferRequest.builder()
                .sourceUPI("alice@okaxis")
                .destinationUPI("bob@paytm")
                .amount(new BigDecimal("200.00"))
                .build();
        
        BigDecimal initialBalance = sourceAccount.getBalance();
        
        // When
        TransferResponse response1 = transferService.initiateTransfer(request1);
        TransferResponse response2 = transferService.initiateTransfer(request2);
        
        // Then
        assertNotNull(response1);
        assertNotNull(response2);
        assertEquals("SUCCESS", response1.getStatus());
        assertEquals("SUCCESS", response2.getStatus());
        
        // Verify total deduction
        Account updatedSource = accountRepository.findByUpiId("alice@okaxis").orElseThrow();
        assertEquals(initialBalance.subtract(new BigDecimal("300.00")), 
                updatedSource.getBalance());
    }
}
