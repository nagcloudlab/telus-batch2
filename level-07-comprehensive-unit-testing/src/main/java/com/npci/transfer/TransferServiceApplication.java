package com.npci.transfer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot Application for Transfer Service
 * 
 * This class is required for Spring Boot tests to work properly.
 * Tests using @WebMvcTest, @DataJpaTest, etc. need to find a @SpringBootConfiguration.
 */
@SpringBootApplication
public class TransferServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(TransferServiceApplication.class, args);
    }
}
