package com.example.factory;

import com.example.repository.AccountRepository;
import com.example.repository.NoSqlAccountRepository;
import com.example.repository.SqlAccountRepository;

public class AccountRepositoryFactory {

    public static AccountRepository getAccountRepository(String type) {
        if (type.equalsIgnoreCase("SQL")) {
            return new SqlAccountRepository();
        } else if (type.equalsIgnoreCase("NoSQL")) {
            return new NoSqlAccountRepository();
        }
        // Add more repository types as needed
        throw new IllegalArgumentException("Unknown repository type: " + type);
    }

}
