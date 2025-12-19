package com.npci.transfer;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String transactionId;
    
    private String sourceUPI;
    private String destinationUPI;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal fee;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal totalDebited;
    
    private String status;
    private String remarks;
    private String errorCode;
    private String errorMessage;
    
    private LocalDateTime timestamp;
}
