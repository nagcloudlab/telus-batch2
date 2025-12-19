package com.npci.transfer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Transaction Entity
 */
@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "transaction_id", unique = true, nullable = false)
    private String transactionId;
    
    @Column(name = "source_upi", nullable = false)
    private String sourceUPI;
    
    @Column(name = "destination_upi", nullable = false)
    private String destinationUPI;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal fee;
    
    @Column(name = "total_debited", precision = 15, scale = 2)
    private BigDecimal totalDebited;
    
    @Column(nullable = false)
    private String status;
    
    @Column
    private String remarks;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
}
