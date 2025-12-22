package contracts.transfers

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Should return 400 when source account has insufficient balance"
    
    request {
        method POST()
        url "/v1/transfers"
        headers {
            contentType(applicationJson())
        }
        body([
            sourceUPI: "charlie@okaxis",
            destinationUPI: "bob@paytm",
            amount: 500.00
        ])
    }
    
    response {
        status BAD_REQUEST()
        headers {
            contentType(applicationJson())
        }
        body([
            timestamp: $(consumer(anyNonEmptyString()), producer(regex('[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}.*'))),
            status: 400,
            error: $(consumer(anyNonEmptyString()), producer("Insufficient Balance")),
            message: $(consumer(anyNonEmptyString()), producer(regex('.*[Ii]nsufficient.*')))
        ])
    }
}
