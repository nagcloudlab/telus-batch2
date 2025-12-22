package com.npci.transfer.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Transfer Request DTO (Data Transfer Object)
 * 
 * Used for transferring data between layers.
 * Includes validation annotations for automatic validation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {
    
    @NotBlank(message = "Source UPI ID is required")
    @Pattern(regexp = "^[a-zA-Z0-9.\\-_]+@[a-zA-Z]+$", 
             message = "Invalid UPI ID format")
    private String sourceUPI;
    
    @NotBlank(message = "Destination UPI ID is required")
    @Pattern(regexp = "^[a-zA-Z0-9.\\-_]+@[a-zA-Z]+$", 
             message = "Invalid UPI ID format")
    private String destinationUPI;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.0", message = "Minimum transfer amount is ₹1")
    @DecimalMax(value = "100000.0", message = "Maximum per-transaction limit is ₹1,00,000")
    private BigDecimal amount;
    
    @Size(max = 255, message = "Remarks cannot exceed 255 characters")
    private String remarks;
}
