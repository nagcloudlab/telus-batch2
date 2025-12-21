package com.npci.transfer.service;

import com.npci.transfer.dto.TransferRequest;
import com.npci.transfer.dto.TransferResponse;
import com.npci.transfer.entity.Account;
import com.npci.transfer.entity.Transaction;
import com.npci.transfer.exception.AccountNotFoundException;
import com.npci.transfer.exception.InsufficientBalanceException;
import com.npci.transfer.exception.InvalidTransferException;
import com.npci.transfer.exception.InvalidAmountException;
import com.npci.transfer.repository.AccountRepository;
import com.npci.transfer.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Transfer Service - ALL SECURITY FIXES + TEST COMPATIBILITY
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransferService {
    
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final FeeCalculator feeCalculator;
    
    private static final BigDecimal MIN_AMOUNT = new BigDecimal("1");
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("100000");
    
    // SECURITY FIX: SecureRandom for cryptographically strong randomness
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    
    @Transactional
    public TransferResponse initiateTransfer(TransferRequest request) {
        // Validate amount
        validateAmount(request.getAmount());
        
        // Validate same account transfer (before DB lookup for efficiency)
        if (request.getSourceUPI().equals(request.getDestinationUPI())) {
            throw new InvalidTransferException("Cannot transfer to the same account");
        }
        
        // Find accounts
        Account sourceAccount = findAccount(request.getSourceUPI(), "Source");
        Account destinationAccount = findAccount(request.getDestinationUPI(), "Destination");
        
        // SECURITY FIX: Sanitized logging
        log.info("Initiating transfer from {} to {} for amount {}",
            sanitizeForLog(request.getSourceUPI()),
            sanitizeForLog(request.getDestinationUPI()),
            request.getAmount());
        
        // Calculate fee
        BigDecimal fee = feeCalculator.calculateFee(request.getAmount());
        BigDecimal totalDebit = request.getAmount().add(fee);
        
        // Validate sufficient balance (with detailed error message for tests)
        validateSufficientBalance(sourceAccount, totalDebit);
        
        // Perform transfer
        sourceAccount.setBalance(sourceAccount.getBalance().subtract(totalDebit));
        destinationAccount.setBalance(destinationAccount.getBalance().add(request.getAmount()));
        
        // Save updated accounts
        accountRepository.save(sourceAccount);
        accountRepository.save(destinationAccount);
        
        // Create transaction
        Transaction transaction = new Transaction();
        transaction.setTransactionId(generateTransactionId());
        transaction.setSourceUPI(request.getSourceUPI());
        transaction.setDestinationUPI(request.getDestinationUPI());
        transaction.setAmount(request.getAmount());
        transaction.setFee(fee);
        transaction.setTotalDebited(totalDebit);
        transaction.setStatus("SUCCESS");
        transaction.setRemarks(request.getRemarks());
        transaction.setTimestamp(LocalDateTime.now());
        
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        // SECURITY FIX: Sanitized logging
        log.info("Transfer completed successfully. Transaction ID: {}",
            sanitizeForLog(savedTransaction.getTransactionId()));
        
        return TransferResponse.builder()
                .transactionId(savedTransaction.getTransactionId())
                .status(savedTransaction.getStatus())
                .sourceUPI(savedTransaction.getSourceUPI())
                .destinationUPI(savedTransaction.getDestinationUPI())
                .amount(savedTransaction.getAmount())
                .fee(savedTransaction.getFee())
                .totalDebited(savedTransaction.getTotalDebited())
                .timestamp(savedTransaction.getTimestamp())
                .remarks(savedTransaction.getRemarks())
                .build();
    }
    
    public BigDecimal checkBalance(String upiId) {
        Account account = findAccount(upiId, "Account");
        return account.getBalance();
    }
    
    public Transaction getTransactionStatus(String transactionId) {
        return transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new InvalidTransferException(
                        "Transaction not found: " + transactionId));
    }
    
    /**
     * Find account with specific error message for source/destination
     */
    private Account findAccount(String upiId, String accountType) {
        return accountRepository.findByUpiId(upiId)
                .orElseThrow(() -> new AccountNotFoundException(
                        accountType + " account not found: " + upiId));
    }
    
    private void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new InvalidAmountException("Amount cannot be null");
        }
        if (amount.compareTo(MIN_AMOUNT) < 0) {
            throw new InvalidAmountException(
                    "Minimum transfer amount is ₹" + MIN_AMOUNT);
        }
        if (amount.compareTo(MAX_AMOUNT) > 0) {
            throw new InvalidAmountException(
                    "Maximum per-transaction limit is ₹" + MAX_AMOUNT);
        }
        
        if (amount.scale() > 2) {
            throw new InvalidAmountException(
                    "Amount cannot have more than 2 decimal places");
        }
    }
    
    /**
     * FIXED: Added detailed error message for tests
     */
    private void validateSufficientBalance(Account account, BigDecimal requiredAmount) {
        if (account.getBalance().compareTo(requiredAmount) < 0) {
            throw new InsufficientBalanceException(
                String.format("Insufficient balance. Available: ₹%s, Required: ₹%s",
                    account.getBalance(), requiredAmount));
        }
    }
    
    /**
     * SECURITY FIX: SecureRandom + correct format for tests
     */
    private String generateTransactionId() {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int randomSuffix = SECURE_RANDOM.nextInt(10000);
        return String.format("TXN-%s-%04d", timestamp, randomSuffix);
    }
    
    /**
     * SECURITY FIX: Prevent CRLF injection
     */
    private String sanitizeForLog(String input) {
        if (input == null) {
            return null;
        }
        return input.replace('\n', '_')
                    .replace('\r', '_')
                    .replace('\t', '_');
    }
}
