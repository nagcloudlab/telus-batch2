package com.npci.transfer.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Singleton PostgreSQL Test Container
 * 
 * Shared across all test classes to avoid container lifecycle issues
 * ONE container for ALL tests - starts once, stops at the end
 */
public abstract class PostgreSQLTestContainer {
    
    // SINGLETON: ONE container for ALL tests
    private static final PostgreSQLContainer<?> POSTGRES_CONTAINER;
    
    static {
        POSTGRES_CONTAINER = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:15-alpine")
        )
        .withDatabaseName("transfer_test_db")
        .withUsername("test_user")
        .withPassword("test_password")
        .withReuse(true);  // Reuse container across test runs
        
        POSTGRES_CONTAINER.start();
    }
    
    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);
    }
}
