package com.npci.transfer.exception;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Error Response DTO with complete defensive copying
 * 
 * SECURITY FIX: Manual constructor to prevent EI_EXPOSE_REP2
 */
@Data
@Builder
@NoArgsConstructor
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private Map<String, String> validationErrors;
    
    /**
     * Constructor with defensive copying
     * SECURITY FIX: Manually implemented to avoid Lombok's @AllArgsConstructor
     * Fixes: EI_EXPOSE_REP2 (constructor)
     */
    public ErrorResponse(LocalDateTime timestamp, int status, String error, 
                         String message, Map<String, String> validationErrors) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        // Defensive copy to prevent external modification
        this.validationErrors = validationErrors != null 
            ? new HashMap<>(validationErrors) 
            : null;
    }
    
    /**
     * Returns defensive copy to prevent external modification
     * SECURITY FIX: Fixes EI_EXPOSE_REP
     */
    public Map<String, String> getValidationErrors() {
        return validationErrors != null 
            ? new HashMap<>(validationErrors) 
            : null;
    }
    
    /**
     * Stores defensive copy to prevent external modification
     * SECURITY FIX: Fixes EI_EXPOSE_REP2
     */
    public void setValidationErrors(Map<String, String> validationErrors) {
        this.validationErrors = validationErrors != null 
            ? new HashMap<>(validationErrors) 
            : null;
    }
}
