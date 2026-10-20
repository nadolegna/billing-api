-- Baseline migration. The full data model arrives in V2 (Day 2).
CREATE TABLE tenants (
    id         UUID PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);
