package com.example;

import com.example.service.UPITransferService;

public class Application {

    public static void main(String[] args) {


        //--------------------------------
        // Init / boot phase
        //--------------------------------
        System.out.println("-".repeat(50));

        // create & assemble components based configuration
        UPITransferService transferService = new UPITransferService();

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
