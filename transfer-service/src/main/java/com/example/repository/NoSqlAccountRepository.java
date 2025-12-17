package com.example.repository;

import com.example.model.Account;

public class NoSqlAccountRepository implements AccountRepository{

    public NoSqlAccountRepository(){
        System.out.println("NoSqlAccountRepository initialized.");
    }

    @Override
    public Account loadAccount(String accountNumber) {
        System.out.println("Loading account " + accountNumber + " from NoSQL database.");
        return new Account(accountNumber," NoSQL User", 1000.0);
    }

    @Override
    public void updateAccount(Account account) {
        System.out.println("Updating account " + account.getNumber() + " in NoSQL database.");
    }
}
