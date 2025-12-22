package com.npci.transfer.integration;

import com.npci.transfer.config.PostgreSQLTestContainer;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base Integration Test Class
 * 
 * Extends PostgreSQLTestContainer for Testcontainers setup
 * Configures REST-Assured for API testing
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class BaseIntegrationTest extends PostgreSQLTestContainer {
    
    @LocalServerPort
    private int port;
    
    @BeforeEach
    void setUpRestAssured() {
        RestAssured.port = port;
        RestAssured.basePath = "/v1";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
}
