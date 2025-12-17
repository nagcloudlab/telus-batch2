package com.example.mock;

import com.example.model.Account;
import com.example.repository.AccountRepository;

public class AccountRepositoryMock implements AccountRepository {
    @Override
    public Account loadAccount(String accountNumber) {
        return new Account(accountNumber, "mock-user", 1000); // Mock account with fixed balance
    }

    @Override
    public void updateAccount(Account account) {

    }
}
