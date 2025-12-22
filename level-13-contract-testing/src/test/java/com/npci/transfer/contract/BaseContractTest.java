package com.npci.transfer.contract;

import com.npci.transfer.config.PostgreSQLTestContainer;
import com.npci.transfer.entity.Account;
import com.npci.transfer.repository.AccountRepository;
import com.npci.transfer.repository.TransactionRepository;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

/**
 * Base Test Class for Spring Cloud Contract Provider Tests
 * 
 * This class configures REST-Assured MockMvc for contract testing
 * and sets up test data for all contract tests.
 */
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public abstract class BaseContractTest extends PostgreSQLTestContainer {
    
    @Autowired
    protected WebApplicationContext context;
    
    @Autowired
    protected AccountRepository accountRepository;
    
    @Autowired
    protected TransactionRepository transactionRepository;
    
    @BeforeEach
    public void setUp() {
        // Configure REST-Assured MockMvc
        RestAssuredMockMvc.webAppContextSetup(context);
        
        // Clean database
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        
        // Create test accounts for contracts
        Account sourceAccount = Account.builder()
                .upiId("alice@okaxis")
                .phone("9876543210")
                .balance(new BigDecimal("10000.00"))
                .dailyLimit(new BigDecimal("100000.00"))
                .dailyUsed(BigDecimal.ZERO)
                .monthlyLimit(new BigDecimal("1000000.00"))
                .monthlyUsed(BigDecimal.ZERO)
                .status("ACTIVE")
                .build();
        accountRepository.save(sourceAccount);
        
        Account destinationAccount = Account.builder()
                .upiId("bob@paytm")
                .phone("9999999999")
                .balance(new BigDecimal("5000.00"))
                .dailyLimit(new BigDecimal("100000.00"))
                .dailyUsed(BigDecimal.ZERO)
                .monthlyLimit(new BigDecimal("1000000.00"))
                .monthlyUsed(BigDecimal.ZERO)
                .status("ACTIVE")
                .build();
        accountRepository.save(destinationAccount);
        
        // Account with low balance for insufficient balance scenarios
        Account poorAccount = Account.builder()
                .upiId("charlie@okaxis")
                .phone("8888888888")
                .balance(new BigDecimal("100.00"))
                .dailyLimit(new BigDecimal("100000.00"))
                .dailyUsed(BigDecimal.ZERO)
                .monthlyLimit(new BigDecimal("1000000.00"))
                .monthlyUsed(BigDecimal.ZERO)
                .status("ACTIVE")
                .build();
        accountRepository.save(poorAccount);
    }
}
