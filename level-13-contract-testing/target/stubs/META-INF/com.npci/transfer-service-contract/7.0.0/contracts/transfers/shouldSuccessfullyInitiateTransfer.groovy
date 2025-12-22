package contracts.transfers

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Should successfully initiate a transfer"
    
    request {
        method POST()
        url "/v1/transfers"
        headers {
            contentType(applicationJson())
        }
        body([
            sourceUPI: "alice@okaxis",
            destinationUPI: "bob@paytm",
            amount: 500.00,
            remarks: "Payment for services"
        ])
    }
    
    response {
        status OK()
        headers {
            contentType(applicationJson())
        }
        body([
            transactionId: $(consumer(anyNonEmptyString()), producer(regex('TXN-[0-9]{8,14}-[0-9]{4}'))),
            sourceUPI: "alice@okaxis",
            destinationUPI: "bob@paytm",
            amount: 500.00,
            fee: $(consumer(anyNumber()), producer(anyNumber())),
            totalDebited: $(consumer(anyNumber()), producer(anyNumber())),
            status: "SUCCESS",
            timestamp: $(consumer(anyNonEmptyString()), producer(regex('[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}.*')))
        ])
    }
}
