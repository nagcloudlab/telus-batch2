package contracts.transfers

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Should return 404 when source account not found"
    
    request {
        method POST()
        url "/v1/transfers"
        headers {
            contentType(applicationJson())
        }
        body([
            sourceUPI: "nonexistent@fake",
            destinationUPI: "bob@paytm",
            amount: 100.00
        ])
    }
    
    response {
        status NOT_FOUND()
        headers {
            contentType(applicationJson())
        }
        body([
            timestamp: $(consumer(anyNonEmptyString()), producer(regex('[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}.*'))),
            status: 404,
            error: $(consumer(anyNonEmptyString()), producer("Account Not Found")),
            message: $(consumer(anyNonEmptyString()), producer(regex('.*[Nn]ot [Ff]ound.*')))
        ])
    }
}
