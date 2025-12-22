package com.npci.transfer.controller;

import com.npci.transfer.dto.TransferRequest;
import com.npci.transfer.dto.TransferResponse;
import com.npci.transfer.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Transfer Controller - HTTP Layer (SECURITY FIXED)
 * 
 * SECURITY FIX: Added CRLF injection prevention in logging
 */
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@Slf4j
public class TransferController {
    
    private final TransferService transferService;
    
    /**
     * Initiates a money transfer.
     */
    @PostMapping("/transfers")
    public ResponseEntity<TransferResponse> initiateTransfer(
            @Valid @RequestBody TransferRequest request) {
        
        // SECURITY FIX: Log with sanitized inputs (prevent CRLF injection)
        log.info("Received transfer request from {} to {}",
            sanitizeForLog(request.getSourceUPI()), 
            sanitizeForLog(request.getDestinationUPI()));
        
        TransferResponse response = transferService.initiateTransfer(request);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "timestamp", LocalDateTime.now().toString(),
            "service", "transfer-service"
        ));
    }
    
    /**
     * Sanitizes input for logging to prevent CRLF injection attacks
     * SECURITY FIX: Fixes CRLF_INJECTION_LOGS
     */
    private String sanitizeForLog(String input) {
        if (input == null) {
            return null;
        }
        return input.replace('\n', '_')
                    .replace('\r', '_')
                    .replace('\t', '_');
    }
}
