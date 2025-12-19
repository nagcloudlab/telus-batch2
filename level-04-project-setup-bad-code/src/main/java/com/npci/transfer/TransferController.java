package com.npci.transfer;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * BAD CODE EXAMPLE - DO NOT FOLLOW THIS PATTERN!
 * 
 * Problems with this code:
 * 1. God Object - Controller does EVERYTHING
 * 2. No separation of concerns
 * 3. Direct database access in controller
 * 4. No validation
 * 5. No error handling
 * 6. Hardcoded values (magic numbers)
 * 7. Long methods (>100 lines)
 * 8. Poor naming (amt, src, dst)
 * 9. No tests
 * 10. Tight coupling to JPA
 */
@RestController
@RequestMapping("/v1")
public class TransferController {
    
    @PersistenceContext
    private EntityManager em;
    
    // BAD: Everything in one massive method
    @PostMapping("/transfers")
    @Transactional
    public Map<String, Object> transfer(@RequestBody Map<String, Object> req) {
        
        // BAD: Poor variable names
        String src = (String) req.get("sourceUPI");
        String dst = (String) req.get("destinationUPI");
        double amt = ((Number) req.get("amount")).doubleValue();
        String rem = (String) req.get("remarks");
        
        // BAD: Magic numbers hardcoded
        BigDecimal amount = BigDecimal.valueOf(amt);
        BigDecimal fee = amount.compareTo(BigDecimal.valueOf(1000)) > 0 
            ? BigDecimal.valueOf(5.0) 
            : BigDecimal.ZERO;
        
        // BAD: Direct SQL in controller
        List<Account> srcAccounts = em.createQuery(
            "SELECT a FROM Account a WHERE a.upiId = :upi", Account.class)
            .setParameter("upi", src)
            .getResultList();
        
        // BAD: No null checks, will crash
        Account sourceAccount = srcAccounts.get(0);
        
        List<Account> dstAccounts = em.createQuery(
            "SELECT a FROM Account a WHERE a.upiId = :upi", Account.class)
            .setParameter("upi", dst)
            .getResultList();
        
        Account destAccount = dstAccounts.get(0);
        
        // BAD: No validation of amount
        // BAD: No checking transaction limits
        // BAD: Complex business logic in controller
        BigDecimal total = amount.add(fee);
        
        if (sourceAccount.getBalance().compareTo(total) < 0) {
            // BAD: Returning error as success response
            Map<String, Object> resp = new HashMap<>();
            resp.put("status", "FAILED");
            resp.put("message", "Insufficient balance");
            return resp;
        }
        
        // BAD: Direct balance manipulation
        sourceAccount.setBalance(sourceAccount.getBalance().subtract(total));
        destAccount.setBalance(destAccount.getBalance().add(amount));
        
        // BAD: Hardcoded transaction ID generation
        String txnId = "TXN-" + LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("yyyyMMdd")) + "-" + 
            new Random().nextInt(999999);
        
        // BAD: Manual object creation instead of builder
        Transaction txn = new Transaction();
        txn.setTransactionId(txnId);
        txn.setSourceUPI(src);
        txn.setDestinationUPI(dst);
        txn.setAmount(amount);
        txn.setFee(fee);
        txn.setTotalDebited(total);
        txn.setStatus("SUCCESS");
        txn.setRemarks(rem);
        txn.setTimestamp(LocalDateTime.now());
        
        em.persist(txn);
        em.flush();
        
        // BAD: Manual response construction
        Map<String, Object> response = new HashMap<>();
        response.put("transactionId", txnId);
        response.put("status", "SUCCESS");
        response.put("sourceUPI", src);
        response.put("destinationUPI", dst);
        response.put("amount", amt);
        response.put("fee", fee.doubleValue());
        response.put("totalDebited", total.doubleValue());
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("remarks", rem);
        
        return response;
    }
    
    // BAD: Get transaction status - also doing too much
    @GetMapping("/transactions/{transactionId}")
    public Map<String, Object> getStatus(@PathVariable String transactionId) {
        
        // BAD: No error handling
        List<Transaction> txns = em.createQuery(
            "SELECT t FROM Transaction t WHERE t.transactionId = :id", Transaction.class)
            .setParameter("id", transactionId)
            .getResultList();
        
        Transaction t = txns.get(0); // BAD: Will crash if not found
        
        Map<String, Object> resp = new HashMap<>();
        resp.put("transactionId", t.getTransactionId());
        resp.put("status", t.getStatus());
        resp.put("sourceUPI", t.getSourceUPI());
        resp.put("destinationUPI", t.getDestinationUPI());
        resp.put("amount", t.getAmount().doubleValue());
        resp.put("fee", t.getFee().doubleValue());
        resp.put("totalDebited", t.getTotalDebited().doubleValue());
        resp.put("timestamp", t.getTimestamp().toString());
        
        return resp;
    }
    
    // BAD: Health check endpoint (at least something simple works!)
    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> h = new HashMap<>();
        h.put("status", "UP");
        h.put("timestamp", LocalDateTime.now().toString());
        return h;
    }
    
    // BAD: Initialize some test data on startup
    @PostMapping("/init-data")
    @Transactional
    public String initData() {
        
        // BAD: No check if data already exists
        Account alice = new Account();
        alice.setUpiId("alice@okaxis");
        alice.setPhoneNumber("+919876543210");
        alice.setEmail("alice@example.com");
        alice.setBalance(BigDecimal.valueOf(10000));
        alice.setDailyLimit(BigDecimal.valueOf(100000));
        alice.setDailyUsed(BigDecimal.ZERO);
        alice.setMonthlyLimit(BigDecimal.valueOf(1000000));
        alice.setMonthlyUsed(BigDecimal.ZERO);
        alice.setStatus("ACTIVE");
        alice.setCreatedAt(LocalDateTime.now());
        alice.setUpdatedAt(LocalDateTime.now());
        
        Account bob = new Account();
        bob.setUpiId("bob@paytm");
        bob.setPhoneNumber("+919876543211");
        bob.setEmail("bob@example.com");
        bob.setBalance(BigDecimal.valueOf(5000));
        bob.setDailyLimit(BigDecimal.valueOf(100000));
        bob.setDailyUsed(BigDecimal.ZERO);
        bob.setMonthlyLimit(BigDecimal.valueOf(1000000));
        bob.setMonthlyUsed(BigDecimal.ZERO);
        bob.setStatus("ACTIVE");
        bob.setCreatedAt(LocalDateTime.now());
        bob.setUpdatedAt(LocalDateTime.now());
        
        em.persist(alice);
        em.persist(bob);
        em.flush();
        
        return "Data initialized: alice@okaxis and bob@paytm";
    }
}
