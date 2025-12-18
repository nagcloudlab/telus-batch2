package com.demo.tramsfer.service;

import com.demo.transfer.model.TransferRequest;
import com.demo.transfer.model.TransferResponse;
import com.demo.transfer.service.TransferService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


//TDD
public class TransferServiceTest {

    TransferService transferService;

    @BeforeEach
    public void setup() {
        transferService = new TransferService();
    }

    @Test
    public void testProcessTransferSuccess() {
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setFromAccount("123456");
        transferRequest.setToAccount("654321");
        transferRequest.setAmount(100.0);
        TransferResponse transferResponse = transferService.processTransfer(transferRequest);
        assertEquals("SUCCESS", transferResponse.getStatus());
    }

    @Test
    @Disabled
    public void testProcessTransferFail() {
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setToAccount("654321");
        transferRequest.setAmount(100.0);
        TransferResponse transferResponse = transferService.processTransfer(transferRequest);
        assertEquals("FAILED", transferResponse.getStatus());
    }

}
