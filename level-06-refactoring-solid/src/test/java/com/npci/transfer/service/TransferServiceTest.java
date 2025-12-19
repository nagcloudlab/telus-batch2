package com.npci.transfer.service;

import com.npci.transfer.dto.TransferRequest;
import com.npci.transfer.dto.TransferResponse;
import com.npci.transfer.entity.Account;
import com.npci.transfer.entity.Transaction;
import com.npci.transfer.exception.*;
import com.npci.transfer.repository.AccountRepository;
import com.npci.transfer.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Transfer Service Tests
 * 
 * Demonstrates testing after applying SOLID principles:
 * - Easy to mock dependencies (D - Dependency Inversion)
 * - Clear responsibilities to test (S - Single Responsibility)
 * - Isolated unit tests
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Transfer Service Tests")
class TransferServiceTest {
    
    @Mock
    private AccountRepository accountRepository;
    
    @Mock
    private TransactionRepository transactionRepository;
    
    @Mock
    private FeeCalculator feeCalculator;
    
    @InjectMocks
    private TransferService transferService;
    
    private Account sourceAccount;
    private Account destinationAccount;
    private TransferRequest request;
    
    @BeforeEach
    void setUp() {
        // Setup source account
        sourceAccount = new Account();
        sourceAccount.setId(1L);
        sourceAccount.setUpiId("alice@okaxis");
        sourceAccount.setPhone("9876543210");
        sourceAccount.setBalance(new BigDecimal("10000"));
        sourceAccount.setStatus("ACTIVE");
        
        // Setup destination account
        destinationAccount = new Account();
        destinationAccount.setId(2L);
        destinationAccount.setUpiId("bob@paytm");
        destinationAccount.setPhone("9876543211");
        destinationAccount.setBalance(new BigDecimal("5000"));
        destinationAccount.setStatus("ACTIVE");
        
        // Setup transfer request
        request = new TransferRequest(
            "alice@okaxis",
            "bob@paytm",
            new BigDecimal("500"),
            "Test transfer"
        );
    }
    
    @Test
    @DisplayName("Should transfer successfully when balance is sufficient")
    void shouldTransferSuccessfully_WhenBalanceIsSufficient() {
        // Arrange
        when(accountRepository.findByUpiId("alice@okaxis"))
            .thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByUpiId("bob@paytm"))
            .thenReturn(Optional.of(destinationAccount));
        when(feeCalculator.calculateFee(any())).thenReturn(BigDecimal.ZERO);
        when(accountRepository.save(any(Account.class))).thenAnswer(i -> i.getArguments()[0]);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);
        
        // Act
        TransferResponse response = transferService.initiateTransfer(request);
        
        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        assertThat(response.getAmount()).isEqualTo(new BigDecimal("500"));
        assertThat(response.getSourceUPI()).isEqualTo("alice@okaxis");
        assertThat(response.getDestinationUPI()).isEqualTo("bob@paytm");
        
        // Verify account balances updated
        assertThat(sourceAccount.getBalance()).isEqualTo(new BigDecimal("9500"));
        assertThat(destinationAccount.getBalance()).isEqualTo(new BigDecimal("5500"));
        
        // Verify repository interactions
        verify(accountRepository, times(2)).save(any(Account.class));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }
    
    @Test
    @DisplayName("Should throw exception when source account not found")
    void shouldThrowException_WhenSourceAccountNotFound() {
        // Arrange
        when(accountRepository.findByUpiId("alice@okaxis"))
            .thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> transferService.initiateTransfer(request))
            .isInstanceOf(AccountNotFoundException.class)
            .hasMessageContaining("Source account not found");
        
        // Verify no account updates
        verify(accountRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Should throw exception when destination account not found")
    void shouldThrowException_WhenDestinationAccountNotFound() {
        // Arrange
        when(accountRepository.findByUpiId("alice@okaxis"))
            .thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByUpiId("bob@paytm"))
            .thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> transferService.initiateTransfer(request))
            .isInstanceOf(AccountNotFoundException.class)
            .hasMessageContaining("Destination account not found");
    }
    
    @Test
    @DisplayName("Should throw exception when insufficient balance")
    void shouldThrowException_WhenInsufficientBalance() {
        // Arrange
        sourceAccount.setBalance(new BigDecimal("100"));
        when(accountRepository.findByUpiId("alice@okaxis"))
            .thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByUpiId("bob@paytm"))
            .thenReturn(Optional.of(destinationAccount));
        when(feeCalculator.calculateFee(any())).thenReturn(BigDecimal.ZERO);
        
        // Act & Assert
        assertThatThrownBy(() -> transferService.initiateTransfer(request))
            .isInstanceOf(InsufficientBalanceException.class)
            .hasMessageContaining("Insufficient balance");
        
        // Verify no account updates
        verify(accountRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Should throw exception when same source and destination")
    void shouldThrowException_WhenSameSourceAndDestination() {
        // Arrange
        request = new TransferRequest(
            "alice@okaxis",
            "alice@okaxis",  // Same as source!
            new BigDecimal("500"),
            "Test"
        );
        
        // Act & Assert
        assertThatThrownBy(() -> transferService.initiateTransfer(request))
            .isInstanceOf(InvalidTransferException.class)
            .hasMessageContaining("Cannot transfer to the same account");
    }
    
    @Test
    @DisplayName("Should throw exception when amount below minimum")
    void shouldThrowException_WhenAmountBelowMinimum() {
        // Arrange
        request.setAmount(new BigDecimal("0.50"));
        
        // Act & Assert
        assertThatThrownBy(() -> transferService.initiateTransfer(request))
            .isInstanceOf(InvalidAmountException.class)
            .hasMessageContaining("Minimum transfer amount");
    }
    
    @Test
    @DisplayName("Should throw exception when amount exceeds maximum")
    void shouldThrowException_WhenAmountExceedsMaximum() {
        // Arrange
        request.setAmount(new BigDecimal("150000"));
        
        // Act & Assert
        assertThatThrownBy(() -> transferService.initiateTransfer(request))
            .isInstanceOf(InvalidAmountException.class)
            .hasMessageContaining("Maximum per-transaction limit");
    }
    
    @Test
    @DisplayName("Should include fee in total debited amount")
    void shouldIncludeFeeInTotalDebited() {
        // Arrange
        BigDecimal fee = new BigDecimal("5.00");
        when(accountRepository.findByUpiId("alice@okaxis"))
            .thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByUpiId("bob@paytm"))
            .thenReturn(Optional.of(destinationAccount));
        when(feeCalculator.calculateFee(any())).thenReturn(fee);
        when(accountRepository.save(any(Account.class))).thenAnswer(i -> i.getArguments()[0]);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);
        
        // Act
        TransferResponse response = transferService.initiateTransfer(request);
        
        // Assert
        assertThat(response.getFee()).isEqualTo(fee);
        assertThat(response.getTotalDebited()).isEqualTo(new BigDecimal("505.00"));
        
        // Verify source debited amount + fee
        assertThat(sourceAccount.getBalance()).isEqualTo(new BigDecimal("9495.00"));
        
        // Verify destination credited only amount (not fee)
        assertThat(destinationAccount.getBalance()).isEqualTo(new BigDecimal("5500"));
    }
    
    @Test
    @DisplayName("Should generate unique transaction ID")
    void shouldGenerateUniqueTransactionId() {
        // Arrange
        when(accountRepository.findByUpiId(anyString()))
            .thenReturn(Optional.of(sourceAccount))
            .thenReturn(Optional.of(destinationAccount));
        when(feeCalculator.calculateFee(any())).thenReturn(BigDecimal.ZERO);
        when(accountRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        when(transactionRepository.save(transactionCaptor.capture()))
            .thenAnswer(i -> i.getArguments()[0]);
        
        // Act
        transferService.initiateTransfer(request);
        
        // Assert
        Transaction savedTransaction = transactionCaptor.getValue();
        assertThat(savedTransaction.getTransactionId()).isNotNull();
        assertThat(savedTransaction.getTransactionId()).startsWith("TXN-");
    }
    
    @Test
    @DisplayName("Should save transaction with correct details")
    void shouldSaveTransactionWithCorrectDetails() {
        // Arrange
        BigDecimal fee = new BigDecimal("5.00");
        when(accountRepository.findByUpiId(anyString()))
            .thenReturn(Optional.of(sourceAccount))
            .thenReturn(Optional.of(destinationAccount));
        when(feeCalculator.calculateFee(any())).thenReturn(fee);
        when(accountRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        when(transactionRepository.save(transactionCaptor.capture()))
            .thenAnswer(i -> i.getArguments()[0]);
        
        // Act
        transferService.initiateTransfer(request);
        
        // Assert
        Transaction savedTransaction = transactionCaptor.getValue();
        assertThat(savedTransaction.getSourceUPI()).isEqualTo("alice@okaxis");
        assertThat(savedTransaction.getDestinationUPI()).isEqualTo("bob@paytm");
        assertThat(savedTransaction.getAmount()).isEqualTo(new BigDecimal("500"));
        assertThat(savedTransaction.getFee()).isEqualTo(fee);
        assertThat(savedTransaction.getTotalDebited()).isEqualTo(new BigDecimal("505.00"));
        assertThat(savedTransaction.getStatus()).isEqualTo("SUCCESS");
        assertThat(savedTransaction.getRemarks()).isEqualTo("Test transfer");
        assertThat(savedTransaction.getTimestamp()).isNotNull();
    }
}
