package com.npci.transfer.performance.level16;

import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

import java.io.IOException;

import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

/**
 * Level 16: Baseline Performance Test
 * 
 * Purpose: Establish baseline performance metrics with normal load
 * 
 * Scenario:
 * - 100 concurrent users
 * - 60 seconds duration
 * - POST /v1/transfers
 * 
 * Success Criteria:
 * - Test completes without errors
 * - HTML report generated for analysis
 * - Manually verify P95 < 200ms in report
 */
public class BaselinePerformanceTest {
    
    private static final String BASE_URL = "http://localhost:8080";
    private static final String TRANSFER_ENDPOINT = BASE_URL + "/v1/transfers";
    
    @Test
    public void baseline100Users60Seconds() throws IOException {
        System.out.println("=".repeat(60));
        System.out.println("LEVEL 16: Baseline Performance Test");
        System.out.println("=".repeat(60));
        System.out.println("Scenario: 100 users, 60 seconds");
        System.out.println("Target: P95 < 200ms, Error rate < 0.1%");
        System.out.println("=".repeat(60));
        System.out.println("\n🚀 Running performance test...\n");
        
        TestPlanStats stats = testPlan(
            threadGroup(100, 60,
                httpSampler(TRANSFER_ENDPOINT)
                    .post(getTransferRequestBody(), ContentType.APPLICATION_JSON)
                    .children(
                        responseAssertion()
                            .containsSubstrings("transactionId")
                    )
            ),
            htmlReporter("target/jmeter-reports/baseline")
        ).run();
        
        // Print Results
        System.out.println("\n" + "=".repeat(60));
        System.out.println("RESULTS");
        System.out.println("=".repeat(60));
        
        long totalSamples = stats.overall().samplesCount();
        long errors = stats.overall().errorsCount();
        double errorRate = (errors / (double) totalSamples) * 100;
        
        System.out.printf("Total Requests: %,d\n", totalSamples);
        System.out.printf("Errors: %d (%.2f%%)\n", errors, errorRate);
        System.out.println("\n📊 Detailed metrics available in HTML report:");
        System.out.println("   target/jmeter-reports/baseline/index.html");
        System.out.println("\n💡 Open the report to view:");
        System.out.println("   - P50, P95, P99 response times");
        System.out.println("   - Response time over time chart");
        System.out.println("   - Throughput metrics");
        System.out.println("   - Error analysis");
        System.out.println("=".repeat(60));
        
        // Basic validation
        if (errorRate < 0.1) {
            System.out.println("\n✅ Error rate acceptable (<0.1%)");
        } else {
            System.out.println("\n⚠️  Warning: Error rate is " + String.format("%.2f%%", errorRate));
        }
        
        if (totalSamples > 1000) {
            System.out.println("✅ Good throughput: " + totalSamples + " requests in 60 seconds");
        }
        
        System.out.println("\n📈 Next: Review the HTML report for detailed percentile analysis");
    }
    
    private String getTransferRequestBody() {
        return "{"
            + "\"sourceUPI\":\"alice@okaxis\","
            + "\"destinationUPI\":\"bob@paytm\","
            + "\"amount\":100.00,"
            + "\"remarks\":\"Performance test\""
            + "}";
    }
}
