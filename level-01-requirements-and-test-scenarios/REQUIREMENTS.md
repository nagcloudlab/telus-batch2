# Transfer Service - Business Requirements

## Overview
Enable secure, real-time money transfer between two UPI accounts for NPCI payment processing.

---

## Functional Requirements

### FR-1: Initiate Transfer
**As a** UPI user  
**I want to** transfer money to another UPI account  
**So that** I can send payments instantly

**Details:**
- User provides: source UPI ID, destination UPI ID, amount, remarks (optional)
- System validates both UPI IDs
- System checks source account balance
- System executes transfer atomically
- System returns transaction ID and status

### FR-2: Validate UPI ID
**As a** system  
**I want to** validate UPI ID format and existence  
**So that** transfers only go to valid accounts

**Details:**
- UPI ID format: `username@bankcode` (e.g., john.doe@okaxis)
- Valid bank codes: okaxis, paytm, ybl, oksbi, etc.
- Check UPI ID exists in system

### FR-3: Check Balance
**As a** system  
**I want to** verify sufficient balance before transfer  
**So that** overdrafts are prevented

**Details:**
- Source account must have: amount + transaction fee
- Transaction fee: ₹0 for amounts ≤ ₹1000, ₹5 for amounts > ₹1000

### FR-4: Execute Transfer
**As a** system  
**I want to** debit source and credit destination atomically  
**So that** money is never lost or duplicated

**Details:**
- ACID transaction (All or Nothing)
- Debit source account
- Credit destination account
- Create audit trail
- Generate unique transaction ID (format: TXN-YYYYMMDD-XXXXXX)

### FR-5: Transaction Limits
**As a** regulator  
**I want to** enforce transaction limits  
**So that** fraud is minimized

**Details:**
- Minimum transfer: ₹1
- Maximum per transaction: ₹1,00,000
- Daily limit per user: ₹1,00,000
- Monthly limit per user: ₹10,00,000

### FR-6: Transaction Status
**As a** user  
**I want to** check my transaction status  
**So that** I know if payment succeeded

**Details:**
- Status values: PENDING, SUCCESS, FAILED, REVERSED
- Real-time status updates
- Transaction history retrieval

---

## Non-Functional Requirements

### NFR-1: Performance
- Response time: < 200ms (95th percentile)
- Throughput: 1000 transactions per second
- Availability: 99.9% uptime (< 8.76 hours downtime/year)

### NFR-2: Security
- All data encrypted in transit (TLS 1.3)
- All data encrypted at rest (AES-256)
- JWT-based authentication
- API rate limiting: 100 requests/minute per user
- Audit logging for all transactions

### NFR-3: Scalability
- Horizontal scaling support
- Database connection pooling
- Stateless application design

### NFR-4: Reliability
- Transaction atomicity guaranteed
- Idempotent APIs (retry-safe)
- Automatic retry for transient failures (max 3 attempts)

### NFR-5: Compliance
- PCI-DSS compliant
- RBI guidelines for digital payments
- NPCI UPI specifications v2.0

---

## Out of Scope (v1.0)
- ❌ Recurring payments/mandates
- ❌ International transfers
- ❌ Multi-currency support
- ❌ Cashback/offers
- ❌ QR code payments
