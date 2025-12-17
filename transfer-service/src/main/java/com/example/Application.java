package com.example;

import com.example.factory.AccountRepositoryFactory;
import com.example.repository.AccountRepository;
import com.example.service.TransferService;
import com.example.service.UPITransferService;

public class Application {

    public static void main(String[] args) {

        //--------------------------------
        // Init / boot phase
        //--------------------------------
        System.out.println("-".repeat(50));


        // create & assemble components based configuration
        AccountRepository sqlAccountRepository = AccountRepositoryFactory.getAccountRepository("sql");
        AccountRepository nosqlAccountRepository = AccountRepositoryFactory.getAccountRepository("nosql");
        TransferService transferService = new UPITransferService(sqlAccountRepository);

        System.out.println("-".repeat(50));
        //--------------------------------
        // Runtime phase
        //--------------------------------

        transferService.transfer(1000.00, "123", "456");
        System.out.println("-".repeat(25));
        transferService.transfer(1000.00, "789", "012");


        System.out.println("-".repeat(50));
        //--------------------------------
        // Shutdown phase
        //--------------------------------
        System.out.println("-".repeat(50));

    }

}
