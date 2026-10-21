# Data model

```mermaid
erDiagram
    TENANTS ||--o{ USERS : has
    TENANTS ||--o{ CUSTOMERS : has
    TENANTS ||--o{ PLANS : offers
    CUSTOMERS ||--o{ SUBSCRIPTIONS : holds
    PLANS ||--o{ SUBSCRIPTIONS : "billed by"
    CUSTOMERS ||--o{ INVOICES : receives
    SUBSCRIPTIONS ||--o{ INVOICES : generates
    INVOICES ||--|{ INVOICE_LINES : contains
    INVOICES ||--o{ PAYMENTS : "settled by"
```

Design notes:
- Every business table has `tenant_id`; all queries are tenant-scoped.
- Money is `NUMERIC(12,2)`, never floating point.
- Invoice numbers are unique per tenant.
- A partial index speeds up the recurring-billing job (active subscriptions that are due).
- A unique index on `(provider, provider_reference)` makes payment webhooks idempotent.
