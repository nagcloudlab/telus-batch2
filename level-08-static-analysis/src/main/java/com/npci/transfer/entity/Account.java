package com.npci.transfer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Account Entity
 */
@Entity
@Table(name = "accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "upi_id", unique = true, nullable = false)
    private String upiId;
    
    @Column(nullable = false)
    private String phone;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;
    
    @Column(name = "daily_limit", precision = 15, scale = 2)
    private BigDecimal dailyLimit;
    
    @Column(name = "daily_used", precision = 15, scale = 2)
    private BigDecimal dailyUsed;
    
    @Column(name = "monthly_limit", precision = 15, scale = 2)
    private BigDecimal monthlyLimit;
    
    @Column(name = "monthly_used", precision = 15, scale = 2)
    private BigDecimal monthlyUsed;
    
    @Column(nullable = false)
    private String status;
}
