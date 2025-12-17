package com.example.repository;

import com.example.model.Account;

/**
 * Repository for managing Account data in a SQL database.
 * author: Dev-1 | Team-1
 */

public class SqlAccountRepository implements AccountRepository {

    public SqlAccountRepository() {
        System.out.println("SqlAccountRepository initialized.");
    }

    public Account loadAccount(String accountNumber) {
        // Simulate loading account from SQL database
        System.out.println("Loading account " + accountNumber + " from SQL database.");
        return new Account(accountNumber, "SQL User", 1000.0);
    }

    public void updateAccount(Account account) {
        // Simulate saving account to SQL database
        System.out.println("Saving account " + account.getNumber() + " to SQL database.");
    }

}
