package com.npci.transfer.util;

import com.npci.transfer.entity.Account;
import com.npci.transfer.dto.TransferRequest;

import java.math.BigDecimal;

/**
 * Test Data Builders
 * 
 * Provides fluent API for creating test data.
 * Makes tests more readable and maintainable.
 * 
 * Benefits:
 * - Reusable across tests
 * - Expressive and readable
 * - Easy to create variations
 * - Reduces duplication
 */
public class TestDataBuilders {
    
    /**
     * Builder for Account entities
     */
    public static class AccountBuilder {
        
        private Long id;
        private String upiId = "test@upi";
        private String phone = "9876543210";
        private BigDecimal balance = new BigDecimal("10000");
        private BigDecimal dailyLimit = new BigDecimal("50000");
        private BigDecimal dailyUsed = BigDecimal.ZERO;
        private BigDecimal monthlyLimit = new BigDecimal("200000");
        private BigDecimal monthlyUsed = BigDecimal.ZERO;
        private String status = "ACTIVE";
        
        public static AccountBuilder anAccount() {
            return new AccountBuilder();
        }
        
        public AccountBuilder withId(Long id) {
            this.id = id;
            return this;
        }
        
        public AccountBuilder withUpiId(String upiId) {
            this.upiId = upiId;
            return this;
        }
        
        public AccountBuilder withPhone(String phone) {
            this.phone = phone;
            return this;
        }
        
        public AccountBuilder withBalance(String balance) {
            this.balance = new BigDecimal(balance);
            return this;
        }
        
        public AccountBuilder withBalance(BigDecimal balance) {
            this.balance = balance;
            return this;
        }
        
        public AccountBuilder withDailyLimit(String dailyLimit) {
            this.dailyLimit = new BigDecimal(dailyLimit);
            return this;
        }
        
        public AccountBuilder withDailyUsed(String dailyUsed) {
            this.dailyUsed = new BigDecimal(dailyUsed);
            return this;
        }
        
        public AccountBuilder withMonthlyLimit(String monthlyLimit) {
            this.monthlyLimit = new BigDecimal(monthlyLimit);
            return this;
        }
        
        public AccountBuilder withMonthlyUsed(String monthlyUsed) {
            this.monthlyUsed = new BigDecimal(monthlyUsed);
            return this;
        }
        
        public AccountBuilder withStatus(String status) {
            this.status = status;
            return this;
        }
        
        // Convenience methods
        
        public AccountBuilder withLowBalance() {
            this.balance = new BigDecimal("100");
            return this;
        }
        
        public AccountBuilder withHighBalance() {
            this.balance = new BigDecimal("500000");
            return this;
        }
        
        public AccountBuilder withZeroBalance() {
            this.balance = BigDecimal.ZERO;
            return this;
        }
        
        public AccountBuilder inactive() {
            this.status = "INACTIVE";
            return this;
        }
        
        public AccountBuilder suspended() {
            this.status = "SUSPENDED";
            return this;
        }
        
        public AccountBuilder withDailyLimitReached() {
            this.dailyUsed = this.dailyLimit;
            return this;
        }
        
        public AccountBuilder with90PercentDailyLimitUsed() {
            this.dailyUsed = this.dailyLimit.multiply(new BigDecimal("0.9"));
            return this;
        }
        
        public Account build() {
            Account account = new Account();
            account.setId(id);
            account.setUpiId(upiId);
            account.setPhone(phone);
            account.setBalance(balance);
            account.setDailyLimit(dailyLimit);
            account.setDailyUsed(dailyUsed);
            account.setMonthlyLimit(monthlyLimit);
            account.setMonthlyUsed(monthlyUsed);
            account.setStatus(status);
            return account;
        }
    }
    
    /**
     * Builder for TransferRequest DTOs
     */
    public static class TransferRequestBuilder {
        
        private String sourceUPI = "alice@okaxis";
        private String destinationUPI = "bob@paytm";
        private BigDecimal amount = new BigDecimal("500");
        private String remarks = "Test transfer";
        
        public static TransferRequestBuilder aTransferRequest() {
            return new TransferRequestBuilder();
        }
        
        public TransferRequestBuilder from(String sourceUPI) {
            this.sourceUPI = sourceUPI;
            return this;
        }
        
        public TransferRequestBuilder to(String destinationUPI) {
            this.destinationUPI = destinationUPI;
            return this;
        }
        
        public TransferRequestBuilder withAmount(String amount) {
            this.amount = new BigDecimal(amount);
            return this;
        }
        
        public TransferRequestBuilder withAmount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }
        
        public TransferRequestBuilder withRemarks(String remarks) {
            this.remarks = remarks;
            return this;
        }
        
        // Convenience methods
        
        public TransferRequestBuilder withMinimumAmount() {
            this.amount = new BigDecimal("1");
            return this;
        }
        
        public TransferRequestBuilder withMaximumAmount() {
            this.amount = new BigDecimal("100000");
            return this;
        }
        
        public TransferRequestBuilder withSmallAmount() {
            this.amount = new BigDecimal("100");
            return this;
        }
        
        public TransferRequestBuilder withLargeAmount() {
            this.amount = new BigDecimal("50000");
            return this;
        }
        
        public TransferRequestBuilder withAmountTriggeringFee() {
            this.amount = new BigDecimal("1500");
            return this;
        }
        
        public TransferRequestBuilder withoutRemarks() {
            this.remarks = null;
            return this;
        }
        
        public TransferRequestBuilder toSameAccount() {
            this.destinationUPI = this.sourceUPI;
            return this;
        }
        
        public TransferRequest build() {
            return new TransferRequest(sourceUPI, destinationUPI, amount, remarks);
        }
    }
}

/**
 * Usage Examples:
 * 
 * // Create account with default values
 * Account account = AccountBuilder.anAccount().build();
 * 
 * // Create account with custom values
 * Account alice = AccountBuilder.anAccount()
 *     .withUpiId("alice@okaxis")
 *     .withBalance("10000")
 *     .build();
 * 
 * // Create account with low balance
 * Account poor = AccountBuilder.anAccount()
 *     .withLowBalance()
 *     .build();
 * 
 * // Create transfer request
 * TransferRequest request = TransferRequestBuilder.aTransferRequest()
 *     .from("alice@okaxis")
 *     .to("bob@paytm")
 *     .withAmount("500")
 *     .build();
 * 
 * // Create transfer with convenience methods
 * TransferRequest minTransfer = TransferRequestBuilder.aTransferRequest()
 *     .withMinimumAmount()
 *     .build();
 */
