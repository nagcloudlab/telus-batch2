package com.npci.transfer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Transfer Response DTO
 * 
 * Represents the response after initiating a transfer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponse {
    
    private String transactionId;
    private String status;
    private String sourceUPI;
    private String destinationUPI;
    private BigDecimal amount;
    private BigDecimal fee;
    private BigDecimal totalDebited;
    private LocalDateTime timestamp;
    private String remarks;
    
    public boolean isSuccess() {
        return "SUCCESS".equalsIgnoreCase(status);
    }
}
