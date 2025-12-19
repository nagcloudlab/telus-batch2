package contracts.transfers

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Should initiate transfer successfully"
    
    request {
        method POST()
        url "/v1/transfers"
        headers {
            contentType(applicationJson())
            header("Authorization", "Bearer valid-token-12345")
        }
        body([
            sourceUPI: "alice@okaxis",
            destinationUPI: "bob@paytm",
            amount: 500.00,
            remarks: "Lunch payment"
        ])
    }
    
    response {
        status 200
        headers {
            contentType(applicationJson())
        }
        body([
            transactionId: $(regex('TXN-\\d{8}-\\d{6}')),
            status: "SUCCESS",
            sourceUPI: "alice@okaxis",
            destinationUPI: "bob@paytm",
            amount: 500.00,
            fee: 0.00,
            totalDebited: 500.00,
            timestamp: $(regex(isoDateTime())),
            remarks: "Lunch payment"
        ])
    }
}
