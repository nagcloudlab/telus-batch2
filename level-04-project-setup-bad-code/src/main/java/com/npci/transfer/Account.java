package com.npci.transfer;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
@Data
@Setter
@Getter
public class Account {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String upiId;
    
    private String phoneNumber;
    private String email;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal balance;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal dailyLimit;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal dailyUsed;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal monthlyLimit;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal monthlyUsed;
    
    private String status;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


}
