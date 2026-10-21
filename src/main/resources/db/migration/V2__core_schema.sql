-- Core billing schema. Every business table carries tenant_id so each query can be tenant-scoped.

CREATE TABLE users (
    id            UUID PRIMARY KEY,
    tenant_id     UUID         NOT NULL REFERENCES tenants (id),
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL CHECK (role IN ('ADMIN', 'STAFF')),
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_users_email UNIQUE (email)
);
CREATE INDEX idx_users_tenant ON users (tenant_id);

CREATE TABLE customers (
    id         UUID PRIMARY KEY,
    tenant_id  UUID         NOT NULL REFERENCES tenants (id),
    name       VARCHAR(255) NOT NULL,
    email      VARCHAR(255) NOT NULL,
    phone      VARCHAR(50),
    address    VARCHAR(500),
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_customers_tenant_email UNIQUE (tenant_id, email)
);
CREATE INDEX idx_customers_tenant ON customers (tenant_id);

CREATE TABLE plans (
    id               UUID PRIMARY KEY,
    tenant_id        UUID           NOT NULL REFERENCES tenants (id),
    name             VARCHAR(255)   NOT NULL,
    description      VARCHAR(1000),
    price            NUMERIC(12, 2) NOT NULL CHECK (price >= 0),
    currency         VARCHAR(3)     NOT NULL,
    billing_interval VARCHAR(20)    NOT NULL CHECK (billing_interval IN ('MONTHLY', 'YEARLY')),
    active           BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMPTZ    NOT NULL DEFAULT now()
);
CREATE INDEX idx_plans_tenant ON plans (tenant_id);

CREATE TABLE subscriptions (
    id                 UUID PRIMARY KEY,
    tenant_id          UUID        NOT NULL REFERENCES tenants (id),
    customer_id        UUID        NOT NULL REFERENCES customers (id),
    plan_id            UUID        NOT NULL REFERENCES plans (id),
    status             VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'PAUSED', 'CANCELLED')),
    start_date         DATE        NOT NULL,
    next_billing_date  DATE        NOT NULL,
    cancelled_at       TIMESTAMPTZ,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_subscriptions_tenant ON subscriptions (tenant_id);
CREATE INDEX idx_subscriptions_customer ON subscriptions (customer_id);
-- The recurring-billing job (Day 6) only looks at active subscriptions that are due.
CREATE INDEX idx_subscriptions_due ON subscriptions (next_billing_date) WHERE status = 'ACTIVE';

CREATE TABLE invoices (
    id              UUID PRIMARY KEY,
    tenant_id       UUID           NOT NULL REFERENCES tenants (id),
    customer_id     UUID           NOT NULL REFERENCES customers (id),
    subscription_id UUID REFERENCES subscriptions (id),
    invoice_number  VARCHAR(50)    NOT NULL,
    status          VARCHAR(20)    NOT NULL CHECK (status IN ('DRAFT', 'SENT', 'PAID', 'OVERDUE', 'VOID')),
    issue_date      DATE           NOT NULL,
    due_date        DATE           NOT NULL,
    currency        VARCHAR(3)     NOT NULL,
    subtotal        NUMERIC(12, 2) NOT NULL CHECK (subtotal >= 0),
    discount_total  NUMERIC(12, 2) NOT NULL DEFAULT 0 CHECK (discount_total >= 0),
    tax_total       NUMERIC(12, 2) NOT NULL DEFAULT 0 CHECK (tax_total >= 0),
    total           NUMERIC(12, 2) NOT NULL CHECK (total >= 0),
    notes           VARCHAR(1000),
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT uq_invoices_tenant_number UNIQUE (tenant_id, invoice_number),
    CONSTRAINT chk_invoices_dates CHECK (due_date >= issue_date)
);
CREATE INDEX idx_invoices_tenant_status ON invoices (tenant_id, status);
CREATE INDEX idx_invoices_tenant_due ON invoices (tenant_id, due_date);
CREATE INDEX idx_invoices_customer ON invoices (customer_id);
CREATE INDEX idx_invoices_subscription ON invoices (subscription_id);

CREATE TABLE invoice_lines (
    id          UUID PRIMARY KEY,
    invoice_id  UUID           NOT NULL REFERENCES invoices (id) ON DELETE CASCADE,
    description VARCHAR(500)   NOT NULL,
    quantity    INTEGER        NOT NULL CHECK (quantity > 0),
    unit_price  NUMERIC(12, 2) NOT NULL CHECK (unit_price >= 0),
    line_total  NUMERIC(12, 2) NOT NULL CHECK (line_total >= 0)
);
CREATE INDEX idx_invoice_lines_invoice ON invoice_lines (invoice_id);

CREATE TABLE payments (
    id                 UUID PRIMARY KEY,
    tenant_id          UUID           NOT NULL REFERENCES tenants (id),
    invoice_id         UUID           NOT NULL REFERENCES invoices (id),
    amount             NUMERIC(12, 2) NOT NULL CHECK (amount > 0),
    currency           VARCHAR(3)     NOT NULL,
    status             VARCHAR(20)    NOT NULL CHECK (status IN ('PENDING', 'SUCCEEDED', 'FAILED')),
    provider           VARCHAR(50)    NOT NULL,
    provider_reference VARCHAR(255),
    paid_at            TIMESTAMPTZ,
    created_at         TIMESTAMPTZ    NOT NULL DEFAULT now()
);
CREATE INDEX idx_payments_tenant ON payments (tenant_id);
CREATE INDEX idx_payments_invoice ON payments (invoice_id);
-- Makes the Stripe webhook (Day 7) idempotent: one payment per provider reference.
CREATE UNIQUE INDEX uq_payments_provider_ref ON payments (provider, provider_reference)
    WHERE provider_reference IS NOT NULL;
