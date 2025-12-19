# Transfer Service - Acceptance Criteria

## Definition of Done
A feature is "DONE" when ALL acceptance criteria are met and verified.

---

## AC-1: Transfer API Endpoint

### Must Have:
✅ POST `/api/v1/transfers` endpoint exists  
✅ Accepts JSON request with: sourceUPI, destinationUPI, amount, remarks  
✅ Returns transaction ID, status, timestamp  
✅ HTTP 200 for success  
✅ HTTP 400 for validation errors  
✅ HTTP 500 for system errors  

### Validation Rules:
✅ sourceUPI is required, format validated  
✅ destinationUPI is required, format validated  
✅ amount is required, numeric, > 0  
✅ remarks is optional, max 100 characters  

---

## AC-2: Balance Validation

### Must Have:
✅ Check balance before debit  
✅ Include transaction fee in balance check  
✅ Reject if insufficient balance  
✅ Clear error message returned  

---

## AC-3: Transaction Atomicity

### Must Have:
✅ Database transaction used  
✅ Debit and credit happen together  
✅ Rollback on any failure  
✅ No partial transactions committed  

---

## AC-4: Transaction Limits

### Must Have:
✅ Minimum amount: ₹1  
✅ Maximum per transaction: ₹1,00,000  
✅ Daily limit per user: ₹1,00,000  
✅ Monthly limit per user: ₹10,00,000  
✅ Appropriate error messages for each limit  

---

## AC-5: Performance SLA

### Must Have:
✅ 95th percentile response time < 200ms  
✅ Support 1000 TPS throughput  
✅ Database connection pooling configured  
✅ No N+1 query problems  

---

## AC-6: Security

### Must Have:
✅ HTTPS/TLS 1.3 enforced  
✅ JWT authentication required  
✅ Input validation on all fields  
✅ SQL injection prevented (parameterized queries)  
✅ XSS prevention (input sanitization)  
✅ Rate limiting: 100 requests/minute per user  

---

## AC-7: Audit Trail

### Must Have:
✅ Every transaction logged with: timestamp, user, amount, status  
✅ Log includes request and response  
✅ Logs are immutable (append-only)  
✅ Sensitive data masked in logs (partial UPI IDs)  

---

## AC-8: Error Handling

### Must Have:
✅ All errors have unique error codes  
✅ User-friendly error messages  
✅ Technical details not exposed to user  
✅ Stack traces logged server-side only  

---

## AC-9: Idempotency

### Must Have:
✅ Same transaction ID returns original result  
✅ No duplicate money transfers  
✅ Idempotency key header supported  

---

## AC-10: Testing Coverage

### Must Have:
✅ Unit test coverage > 80%  
✅ All happy path scenarios tested  
✅ All edge cases tested  
✅ All error scenarios tested  
✅ Integration tests for API endpoints  
✅ Performance tests for SLA validation  
