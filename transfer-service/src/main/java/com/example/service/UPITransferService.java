package com.example.service;

import com.example.factory.AccountRepositoryFactory;
import com.example.repository.AccountRepository;
import com.example.repository.SqlAccountRepository;

/**
 * Service for handling UPI transfers between accounts.
 * <p>
 * author: Dev-2 | Team-2
 *
 */

/*

    design issues
    ---------------
    -> tight coupling with SqlAccountRepository makes it hard to switch to a different data source &
       difficult to unit test
        => can't extend/swap repository without modifying this class
        => dev & bug-fix becomes slower

    performance issues
    --------------------
    -> Each transfer creates a new instance of SqlAccountRepository,
       which may lead to unnecessary overhead
       => resource use higher than needed
       => performance degradation under high load

    why design issue occurs?
    -----------------------------
    -> dependent managing lifecycle of its dependencies

    solution to design issue
    -----------------------
    -> Don't create, get from  factory  ( factory pattern )

    still performance issue?
    -------------------------
    -> on each transfer, new instance of repository created with Factory

    solution to performance issue
    ---------------------------
    -> don't lookup/get instance from factory directly , inject by third-party

    -----

    Asking dependent don't create/find dependencies, inject from outside
    ( Dependency Inversion Principle  aka Inversion of Control  aka Dependency Injection )

    -------


    SOLID principles followed
    ------------------------
    -> Single Responsibility Principle : UPITransferService only handles transfer logic
    -> Open/Closed Principle : can extend repository types without modifying this class
    -> Liskov Substitution Principle : any AccountRepository implementation can be used
    -> Interface Segregation Principle : depends only on AccountRepository interface
    -> Dependency Inversion Principle : depends on abstraction (AccountRepository), not concrete implementation



 */

public class UPITransferService implements TransferService {

    private AccountRepository accountRepository;

    // Constructor Injection
    public UPITransferService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
        System.out.println("UPITransferService initialized.");
    }

    // tps: transactions per second
    public void transfer(double amount, String fromAccountNumber, String toAccountNumber) {
        System.out.println("Initiating UPI transfer of " + amount + " from " + fromAccountNumber + " to " + toAccountNumber);

        //var accountRepository = new SqlAccountRepository(); // Don't create
        //var accountRepository = AccountRepositoryFactory.getAccountRepository("nosql"); // Don't lookup/get , inject from outside

        var fromAccount = accountRepository.loadAccount(fromAccountNumber);
        var toAccount = accountRepository.loadAccount(toAccountNumber);

        fromAccount.debit(amount);
        toAccount.credit(amount);

        accountRepository.updateAccount(fromAccount);
        accountRepository.updateAccount(toAccount);

        System.out.println("UPI transfer completed successfully.");

    }

}
