package com.demo.transfer;

import com.demo.transfer.model.TransferRequest;
import com.demo.transfer.model.TransferResponse;
import com.demo.transfer.service.TransferService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {

        SpringApplication.run(Application.class,args);

    }

}
