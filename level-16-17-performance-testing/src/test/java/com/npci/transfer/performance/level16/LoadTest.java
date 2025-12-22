package com.npci.transfer.performance.level16;

import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

import java.io.IOException;
import java.time.Duration;

import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

/**
 * Level 16: Load Test
 * 
 * Purpose: Test system under sustained high load
 * 
 * Scenario:
 * - Ramp up to 1000 users over 60 seconds
 * - Hold 1000 users for 5 minutes
 * - POST /v1/transfers
 * 
 * Success Criteria:
 * - Test completes without errors
 * - Error rate < 1%
 * - HTML report generated for analysis
 */
public class LoadTest {
    
    private static final String BASE_URL = "http://localhost:8080";
    private static final String TRANSFER_ENDPOINT = BASE_URL + "/v1/transfers";
    
    @Test
    public void load1000Users5Minutes() throws IOException {
        System.out.println("=".repeat(60));
        System.out.println("LEVEL 16: Load Test");
        System.out.println("=".repeat(60));
        System.out.println("Scenario: Ramp to 1000 users, hold for 5 minutes");
        System.out.println("Target: P95 < 500ms, Error rate < 1%");
        System.out.println("=".repeat(60));
        System.out.println("\n🚀 Running load test (this will take ~6 minutes)...\n");
        
        TestPlanStats stats = testPlan(
            threadGroup()
                .rampToAndHold(1000, Duration.ofSeconds(60), Duration.ofMinutes(5))
                .children(
                    httpSampler(TRANSFER_ENDPOINT)
                        .post(getTransferRequestBody(), ContentType.APPLICATION_JSON)
                        .children(
                            responseAssertion()
                                .containsSubstrings("transactionId")
                        )
                ),
            htmlReporter("target/jmeter-reports/load")
        ).run();
        
        // Print Results
        System.out.println("\n" + "=".repeat(60));
        System.out.println("RESULTS");
        System.out.println("=".repeat(60));
        
        long totalSamples = stats.overall().samplesCount();
        long errors = stats.overall().errorsCount();
        double errorRate = (errors / (double) totalSamples) * 100;
        double throughput = totalSamples / 360.0; // 6 minutes
        
        System.out.printf("Total Requests: %,d\n", totalSamples);
        System.out.printf("Errors: %d (%.2f%%)\n", errors, errorRate);
        System.out.printf("Average Throughput: %.1f requests/sec\n", throughput);
        System.out.println("\n📊 Detailed metrics available in HTML report:");
        System.out.println("   target/jmeter-reports/load/index.html");
        System.out.println("\n💡 Open the report to view:");
        System.out.println("   - P50, P90, P95, P99 response times");
        System.out.println("   - Response time trends");
        System.out.println("   - Active threads over time");
        System.out.println("   - Throughput analysis");
        System.out.println("=".repeat(60));
        
        // Validate Load Test Criteria
        System.out.println("\nLoad Test Validation:");
        
        if (errorRate < 1.0) {
            System.out.println("✅ Error rate acceptable: " + String.format("%.2f%%", errorRate) + " (<1%)");
        } else {
            System.out.println("⚠️  Warning: Error rate is " + String.format("%.2f%%", errorRate) + " (>1%)");
        }
        
        if (throughput > 100) {
            System.out.println("✅ Good throughput: " + String.format("%.1f", throughput) + " TPS");
        } else {
            System.out.println("⚠️  Low throughput: " + String.format("%.1f", throughput) + " TPS");
        }
        
        System.out.println("\n📈 Next: Review the HTML report for detailed analysis");
        System.out.println("       Check if P95 < 500ms in the Statistics table");
    }
    
    private String getTransferRequestBody() {
        return "{"
            + "\"sourceUPI\":\"alice@okaxis\","
            + "\"destinationUPI\":\"bob@paytm\","
            + "\"amount\":100.00,"
            + "\"remarks\":\"Load test\""
            + "}";
    }
}
