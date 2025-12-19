# Transfer Service - Performance SLAs

## Response Time SLAs

| Operation | p50 | p95 | p99 | Max |
|-----------|-----|-----|-----|-----|
| Transfer API | < 100ms | < 200ms | < 500ms | < 1s |
| Balance Check | < 50ms | < 100ms | < 200ms | < 500ms |
| Transaction Status | < 30ms | < 100ms | < 200ms | < 500ms |
| Transaction History | < 100ms | < 200ms | < 500ms | < 1s |

---

## Throughput SLAs

| Metric | Target | Measurement |
|--------|--------|-------------|
| Transactions per second (TPS) | 1,000 | Peak load |
| Concurrent users | 10,000 | Sustained |
| Database connections | 50-100 | Connection pool |
| API requests/minute | 60,000 | Rate limit total |

---

## Availability SLAs

| Metric | Target | Downtime/Year |
|--------|--------|---------------|
| Uptime | 99.9% | < 8.76 hours |
| Planned maintenance window | < 4 hours/month | Announced 7 days prior |
| Mean Time To Recovery (MTTR) | < 15 minutes | From incident detection |
| Mean Time Between Failures (MTBF) | > 30 days | Production incidents |

---

## Scalability Targets

| Metric | Current | 6 Months | 12 Months |
|--------|---------|----------|-----------|
| TPS | 1,000 | 5,000 | 10,000 |
| Database size | 1 GB | 10 GB | 50 GB |
| Concurrent users | 10,000 | 50,000 | 100,000 |

---

## Database Performance

| Metric | Target |
|--------|--------|
| Query response time | < 50ms (p95) |
| Connection acquisition | < 10ms |
| Transaction commit time | < 20ms |
| Index lookup time | < 5ms |

---

## Infrastructure Limits

| Resource | Limit | Alert Threshold |
|----------|-------|-----------------|
| CPU usage | < 70% | 60% |
| Memory usage | < 80% | 70% |
| Disk I/O | < 80% | 70% |
| Network bandwidth | < 70% | 60% |

---

## Measurement & Monitoring

### How SLAs are Measured:
1. **Application Metrics**: Micrometer + Prometheus
2. **Database Metrics**: PostgreSQL stats + pg_stat_statements
3. **Infrastructure Metrics**: CloudWatch/Azure Monitor
4. **User Experience Metrics**: Synthetic monitoring every 1 minute

### Reporting:
- Real-time dashboard (Grafana)
- Daily SLA report
- Weekly trend analysis
- Monthly SLA review with stakeholders

### Alerts:
- Response time > 200ms (p95) for 5 minutes → Page on-call
- TPS drops below 500 → Alert team
- Error rate > 1% → Page on-call
- Availability < 99.9% monthly → Executive escalation
