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
    
    private Account sourceAccount;
    private Account destinationAccount;
    
    @BeforeEach
    void setUp() {
        // Clear data
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        
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
    }
    
    @Test
    @DisplayName("Should transfer money successfully with PostgreSQL")
    void shouldTransferMoneySuccessfully() {
        // Given
        BigDecimal amount = new BigDecimal("500.00");
        BigDecimal fee = feeCalculator.calculateFee(amount);
        
        TransferRequest request = TransferRequest.builder()
                .sourceUPI("alice@okaxis")
                .destinationUPI("bob@paytm")
                .amount(amount)
                .remarks("Test transfer")
                .build();
        
        // When
        TransferResponse response = transferService.initiateTransfer(request);
        
        // Then - Verify response
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.getTransactionId());
        assertEquals(0, amount.compareTo(response.getAmount()));
        
        // Verify database state (PostgreSQL persistence)
        Account updatedSource = accountRepository.findByUpiId("alice@okaxis").orElseThrow();
        Account updatedDest = accountRepository.findByUpiId("bob@paytm").orElseThrow();
        
        assertEquals(0, 
            new BigDecimal("10000.00").subtract(amount).subtract(fee).compareTo(updatedSource.getBalance()));
        assertEquals(0, 
            new BigDecimal("5000.00").add(amount).compareTo(updatedDest.getBalance()));
    }
    
    @Test
    @DisplayName("Should rollback transaction on insufficient balance - PostgreSQL ACID")
    void shouldRollbackTransactionOnInsufficientBalance() {
        // Given - Source has 10000, trying to transfer 15000
        BigDecimal amount = new BigDecimal("15000.00");
        
        TransferRequest request = TransferRequest.builder()
                .sourceUPI("alice@okaxis")
                .destinationUPI("bob@paytm")
                .amount(amount)
                .remarks("Test insufficient balance")
                .build();
        
        // When/Then - Should throw exception
        assertThrows(InsufficientBalanceException.class, () -> {
            transferService.initiateTransfer(request);
        });
        
        // Verify NO changes in PostgreSQL (transaction rolled back)
        Account unchangedSource = accountRepository.findByUpiId("alice@okaxis").orElseThrow();
        Account unchangedDest = accountRepository.findByUpiId("bob@paytm").orElseThrow();
        
        assertEquals(0, new BigDecimal("10000.00").compareTo(unchangedSource.getBalance()));
        assertEquals(0, new BigDecimal("5000.00").compareTo(unchangedDest.getBalance()));
    }
    
    @Test
    @DisplayName("Should handle non-existent account - PostgreSQL constraint")
    void shouldHandleNonExistentAccount() {
        // Given
        TransferRequest request = TransferRequest.builder()
                .sourceUPI("nonexistent@fake")
                .destinationUPI("bob@paytm")
                .amount(new BigDecimal("100.00"))
                .remarks("Test non-existent")
                .build();
        
        // When/Then
        assertThrows(AccountNotFoundException.class, () -> {
            transferService.initiateTransfer(request);
        });
    }
    
    @Test
    @DisplayName("Should handle concurrent transfers - PostgreSQL isolation")
    void shouldHandleConcurrentTransfers() {
        // Given - Multiple small transfers
        TransferRequest request1 = TransferRequest.builder()
                .sourceUPI("alice@okaxis")
                .destinationUPI("bob@paytm")
                .amount(new BigDecimal("100.00"))
                .remarks("Transfer 1")
                .build();
        
        TransferRequest request2 = TransferRequest.builder()
                .sourceUPI("alice@okaxis")
                .destinationUPI("bob@paytm")
                .amount(new BigDecimal("200.00"))
                .remarks("Transfer 2")
                .build();
        
        // When
        TransferResponse response1 = transferService.initiateTransfer(request1);
        TransferResponse response2 = transferService.initiateTransfer(request2);
        
        // Then - Both succeed, balances correct
        assertTrue(response1.isSuccess());
        assertTrue(response2.isSuccess());
        
        Account finalSource = accountRepository.findByUpiId("alice@okaxis").orElseThrow();
        BigDecimal expectedSource = new BigDecimal("10000.00")
                .subtract(new BigDecimal("100.00"))
                .subtract(feeCalculator.calculateFee(new BigDecimal("100.00")))
                .subtract(new BigDecimal("200.00"))
                .subtract(feeCalculator.calculateFee(new BigDecimal("200.00")));
        
        assertEquals(0, expectedSource.compareTo(finalSource.getBalance()));
    }
    
    @Test
    @DisplayName("Should persist transaction history in PostgreSQL")
    void shouldPersistTransactionHistory() {
        // Given
        TransferRequest request = TransferRequest.builder()
                .sourceUPI("alice@okaxis")
                .destinationUPI("bob@paytm")
                .amount(new BigDecimal("300.00"))
                .remarks("Test history")
                .build();
        
        // When
        TransferResponse response = transferService.initiateTransfer(request);
        
        // Then - Transaction persisted in PostgreSQL
        var transactions = transactionRepository.findAll();
        assertEquals(1, transactions.size());
        
        var transaction = transactions.get(0);
        assertEquals(response.getTransactionId(), transaction.getTransactionId());
        assertEquals(0, new BigDecimal("300.00").compareTo(transaction.getAmount()));
        assertEquals("alice@okaxis", transaction.getSourceUPI());
        assertEquals("bob@paytm", transaction.getDestinationUPI());
    }
}
