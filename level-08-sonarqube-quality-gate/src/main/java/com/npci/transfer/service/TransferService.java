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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * Transfer Service - Business Logic Layer
 * 
 * Responsibilities:
 * - Execute transfer business logic
 * - Validate transfers
 * - Calculate fees
 * - Create transactions
 * 
 * Follows Single Responsibility Principle (S in SOLID)
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
    
    /**
     * Initiates a money transfer between two accounts.
     * 
     * @param request Transfer request containing source, destination, amount
     * @return TransferResponse with transaction details
     * @throws AccountNotFoundException if source or destination account not found
     * @throws InsufficientBalanceException if source account has insufficient balance
     * @throws InvalidTransferException if transfer is invalid
     */
    @Transactional
    public TransferResponse initiateTransfer(TransferRequest request) {
        log.info("Initiating transfer from {} to {} for amount {}",
            request.getSourceUPI(), request.getDestinationUPI(), request.getAmount());
        
        // Step 1: Validate request
        validateTransfer(request);
        
        // Step 2: Find accounts
        Account source = findAccount(request.getSourceUPI(), "Source");
        Account destination = findAccount(request.getDestinationUPI(), "Destination");
        
        // Step 3: Calculate fee
        BigDecimal fee = feeCalculator.calculateFee(request.getAmount());
        BigDecimal totalDebited = request.getAmount().add(fee);
        
        log.debug("Fee calculated: {}, Total to debit: {}", fee, totalDebited);
        
        // Step 4: Check balance
        validateBalance(source, totalDebited);
        
        // Step 5: Execute transfer
        executeTransfer(source, destination, request.getAmount(), totalDebited);
        
        // Step 6: Save accounts
        accountRepository.save(source);
        accountRepository.save(destination);
        
        // Step 7: Create transaction record
        Transaction transaction = createTransaction(request, fee, totalDebited);
        transactionRepository.save(transaction);
        
        log.info("Transfer completed successfully. Transaction ID: {}", 
            transaction.getTransactionId());
        
        // Step 8: Build response
        return buildResponse(transaction);
    }
    
    /**
     * Validates the transfer request.
     */
    private void validateTransfer(TransferRequest request) {
        // Check if source and destination are the same
        if (request.getSourceUPI().equals(request.getDestinationUPI())) {
            throw new InvalidTransferException("Cannot transfer to the same account");
        }
        
        // Validate amount range
        if (request.getAmount().compareTo(MIN_AMOUNT) < 0) {
            throw new InvalidAmountException(
                String.format("Minimum transfer amount is ₹%s", MIN_AMOUNT));
        }
        
        if (request.getAmount().compareTo(MAX_AMOUNT) > 0) {
            throw new InvalidAmountException(
                String.format("Maximum per-transaction limit is ₹%s", MAX_AMOUNT));
        }
    }
    
    /**
     * Finds an account by UPI ID.
     */
    private Account findAccount(String upiId, String accountType) {
        return accountRepository.findByUpiId(upiId)
            .orElseThrow(() -> new AccountNotFoundException(
                String.format("%s account not found: %s", accountType, upiId)));
    }
    
    /**
     * Validates if source account has sufficient balance.
     */
    private void validateBalance(Account source, BigDecimal totalRequired) {
        if (source.getBalance().compareTo(totalRequired) < 0) {
            throw new InsufficientBalanceException(
                String.format("Insufficient balance. Available: ₹%s, Required: ₹%s",
                    source.getBalance(), totalRequired));
        }
    }
    
    /**
     * Executes the actual transfer by updating account balances.
     */
    private void executeTransfer(Account source, Account destination, 
                                 BigDecimal amount, BigDecimal totalDebited) {
        // Debit from source (including fee)
        source.setBalance(source.getBalance().subtract(totalDebited));
        
        // Credit to destination (amount only, fee is kept by system)
        destination.setBalance(destination.getBalance().add(amount));
        
        log.debug("Source new balance: {}, Destination new balance: {}",
            source.getBalance(), destination.getBalance());
    }
    
    /**
     * Creates a transaction record.
     */
    private Transaction createTransaction(TransferRequest request, 
                                         BigDecimal fee, BigDecimal totalDebited) {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(generateTransactionId());
        transaction.setSourceUPI(request.getSourceUPI());
        transaction.setDestinationUPI(request.getDestinationUPI());
        transaction.setAmount(request.getAmount());
        transaction.setFee(fee);
        transaction.setTotalDebited(totalDebited);
        transaction.setStatus("SUCCESS");
        transaction.setRemarks(request.getRemarks());
        transaction.setTimestamp(LocalDateTime.now());
        return transaction;
    }
    
    /**
     * Generates a unique transaction ID.
     */
    private String generateTransactionId() {
        LocalDateTime now = LocalDateTime.now();
        String datePart = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String timePart = now.format(DateTimeFormatter.ofPattern("HHmmss"));
        String randomPart = String.format("%04d", new Random().nextInt(9999));
        return "TXN-" + datePart + "-" + timePart + "-" + randomPart;
    }
    
    /**
     * Builds the transfer response.
     */
    private TransferResponse buildResponse(Transaction transaction) {
        return TransferResponse.builder()
            .transactionId(transaction.getTransactionId())
            .status(transaction.getStatus())
            .sourceUPI(transaction.getSourceUPI())
            .destinationUPI(transaction.getDestinationUPI())
            .amount(transaction.getAmount())
            .fee(transaction.getFee())
            .totalDebited(transaction.getTotalDebited())
            .timestamp(transaction.getTimestamp())
            .remarks(transaction.getRemarks())
            .build();
    }
}
