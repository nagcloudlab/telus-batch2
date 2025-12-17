package com.example.service;

import com.example.repository.SqlAccountRepository;

/**
 * Service for handling UPI transfers between accounts.
 * <p>
 * author: Dev-2 | Team-2
 *
 */

public class UPITransferService {

    public UPITransferService() {
        System.out.println("UPITransferService initialized.");
    }

    public void transfer(double amount, String fromAccountNumber, String toAccountNumber) {
        System.out.println("Initiating UPI transfer of " + amount + " from " + fromAccountNumber + " to " + toAccountNumber);

        SqlAccountRepository accountRepository = new SqlAccountRepository();

        var fromAccount = accountRepository.loadAccount(fromAccountNumber);
        var toAccount = accountRepository.loadAccount(toAccountNumber);

        fromAccount.debit(amount);
        toAccount.credit(amount);

        accountRepository.updateAccount(fromAccount);
        accountRepository.updateAccount(toAccount);

        System.out.println("UPI transfer completed successfully.");

    }

}
