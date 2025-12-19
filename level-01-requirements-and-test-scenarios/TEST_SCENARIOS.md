# Transfer Service - Test Scenarios

## Format
All scenarios use Given-When-Then (Gherkin) format for clarity and automation readiness.

---

## TS-1: Successful Transfer (Happy Path)

**Given** User has UPI ID "alice@okaxis" with balance ₹10,000  
**And** Destination UPI ID "bob@paytm" exists  
**When** User transfers ₹500 to "bob@paytm" with remarks "Lunch payment"  
**Then** Transaction status is "SUCCESS"  
**And** Alice's balance is ₹9,500  
**And** Bob's balance is ₹10,500  
**And** Transaction ID is generated (format: TXN-YYYYMMDD-XXXXXX)  
**And** Response time is < 200ms  

---

## TS-2: Insufficient Balance

**Given** User has UPI ID "alice@okaxis" with balance ₹100  
**When** User transfers ₹500 to "bob@paytm"  
**Then** Transaction status is "FAILED"  
**And** Error code is "INSUFFICIENT_BALANCE"  
**And** Error message is "Insufficient balance. Available: ₹100, Required: ₹500"  
**And** Alice's balance remains ₹100  
**And** Bob's balance is unchanged  

---

## TS-3: Invalid Source UPI ID

**Given** User provides invalid UPI ID "alice@invalid"  
**When** User attempts to transfer ₹500  
**Then** Transaction status is "FAILED"  
**And** Error code is "INVALID_SOURCE_UPI"  
**And** Error message is "Source UPI ID not found"  
**And** No money is debited  

---

## TS-4: Invalid Destination UPI ID

**Given** User has UPI ID "alice@okaxis" with balance ₹10,000  
**And** Destination UPI ID "nonexistent@paytm" does not exist  
**When** User transfers ₹500 to "nonexistent@paytm"  
**Then** Transaction status is "FAILED"  
**And** Error code is "INVALID_DESTINATION_UPI"  
**And** Error message is "Destination UPI ID not found"  
**And** Alice's balance remains ₹10,000  

---

## TS-5: Invalid UPI ID Format

**Given** User provides malformed UPI ID "alice.okaxis" (missing @)  
**When** User attempts to transfer ₹500  
**Then** Transaction status is "FAILED"  
**And** Error code is "INVALID_UPI_FORMAT"  
**And** Error message is "UPI ID must be in format: username@bankcode"  

---

## TS-6: Amount Below Minimum Limit

**Given** User has UPI ID "alice@okaxis" with balance ₹10,000  
**When** User transfers ₹0.50 to "bob@paytm"  
**Then** Transaction status is "FAILED"  
**And** Error code is "AMOUNT_BELOW_MINIMUM"  
**And** Error message is "Minimum transfer amount is ₹1"  

---

## TS-7: Amount Exceeds Per-Transaction Limit

**Given** User has UPI ID "alice@okaxis" with balance ₹5,00,000  
**When** User transfers ₹1,50,000 to "bob@paytm"  
**Then** Transaction status is "FAILED"  
**And** Error code is "AMOUNT_EXCEEDS_LIMIT"  
**And** Error message is "Maximum per-transaction limit is ₹1,00,000"  

---

## TS-8: Daily Limit Exceeded

**Given** User "alice@okaxis" has already transferred ₹90,000 today  
**And** Balance is ₹50,000  
**When** User transfers ₹15,000 to "bob@paytm"  
**Then** Transaction status is "FAILED"  
**And** Error code is "DAILY_LIMIT_EXCEEDED"  
**And** Error message is "Daily limit exceeded. Limit: ₹1,00,000, Used: ₹90,000"  

---

## TS-9: Idempotency - Duplicate Transaction ID

**Given** Transaction with ID "TXN-20241220-123456" already processed  
**When** Same transaction ID is submitted again  
**Then** Transaction is not processed again  
**And** Original transaction details are returned  
**And** Response includes "DUPLICATE_TRANSACTION" warning  

---

## TS-10: Database Connection Failure

**Given** User has valid UPI ID and balance  
**And** Database is unavailable  
**When** User attempts transfer  
**Then** Transaction status is "FAILED"  
**And** Error code is "SERVICE_UNAVAILABLE"  
**And** Error message is "Service temporarily unavailable. Please retry."  
**And** No partial transaction is committed  

---

## TS-11: Concurrent Transfers (Race Condition)

**Given** User "alice@okaxis" has balance ₹1,000  
**When** Two simultaneous transfer requests for ₹800 each are submitted  
**Then** Only one transfer succeeds  
**And** Second transfer fails with "INSUFFICIENT_BALANCE"  
**And** Final balance is ₹200 (not negative)  

---

## TS-12: Transaction Timeout

**Given** User has valid UPI ID and balance  
**And** Transaction processing takes > 30 seconds  
**When** Timeout threshold is reached  
**Then** Transaction status is "FAILED"  
**And** Error code is "TRANSACTION_TIMEOUT"  
**And** Money is rolled back to source account  

---

## TS-13: Same Source and Destination

**Given** User has UPI ID "alice@okaxis"  
**When** User transfers to same UPI ID "alice@okaxis"  
**Then** Transaction status is "FAILED"  
**And** Error code is "SAME_SOURCE_DESTINATION"  
**And** Error message is "Cannot transfer to the same account"  

---

## TS-14: Negative Amount

**Given** User attempts to transfer with amount "-500"  
**When** Request is validated  
**Then** Transaction status is "FAILED"  
**And** Error code is "INVALID_AMOUNT"  
**And** Error message is "Amount must be positive"  

---

## TS-15: Transaction with Fee Calculation

**Given** User has UPI ID "alice@okaxis" with balance ₹10,000  
**When** User transfers ₹1,500 to "bob@paytm"  
**Then** Transaction fee is ₹5 (amount > ₹1000)  
**And** Total debited from Alice: ₹1,505  
**And** Bob receives: ₹1,500  
**And** Alice's final balance: ₹8,495  

---

## TS-16: Retrieve Transaction Status

**Given** Transaction ID "TXN-20241220-123456" exists  
**When** User queries transaction status  
**Then** Response includes: transaction ID, status, amount, timestamp, source UPI, destination UPI  
**And** Response time < 100ms  

---

## TS-17: Transaction History

**Given** User "alice@okaxis" has 10 transactions in history  
**When** User requests transaction history with pagination (page=1, size=5)  
**Then** Response includes 5 transactions  
**And** Transactions are sorted by timestamp (newest first)  
**And** Response includes total count = 10  

---

## TS-18: Authentication Failure

**Given** User provides invalid/expired JWT token  
**When** User attempts transfer  
**Then** HTTP status is 401 Unauthorized  
**And** Error message is "Invalid or expired authentication token"  

---

## TS-19: Rate Limiting

**Given** User has made 100 API requests in last minute  
**When** User makes 101st request  
**Then** HTTP status is 429 Too Many Requests  
**And** Response header includes "Retry-After: 60"  
**And** Error message is "Rate limit exceeded. Try after 60 seconds"  

---

## TS-20: Special Characters in Remarks

**Given** User provides remarks with special characters: "<script>alert('xss')</script>"  
**When** Transfer is processed  
**Then** Transaction succeeds  
**And** Remarks are sanitized and stored safely  
**And** No script execution occurs  
