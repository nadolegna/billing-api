-- Demo data, loaded only with the "dev" profile. Users are added on Day 3 together with password hashing.

INSERT INTO tenants (id, name) VALUES
    ('11111111-1111-1111-1111-111111111111', 'Acme Studio');

INSERT INTO customers (id, tenant_id, name, email, phone, address) VALUES
    ('22222222-0000-0000-0000-000000000001', '11111111-1111-1111-1111-111111111111', 'Northwind Traders', 'billing@northwind.example', '+1 555 0101', '12 Harbor St, Portland'),
    ('22222222-0000-0000-0000-000000000002', '11111111-1111-1111-1111-111111111111', 'Globex Corporation', 'ap@globex.example', '+1 555 0102', '500 Market Ave, Austin'),
    ('22222222-0000-0000-0000-000000000003', '11111111-1111-1111-1111-111111111111', 'Initech', 'finance@initech.example', NULL, '1 Office Park, Denver');

INSERT INTO plans (id, tenant_id, name, description, price, currency, billing_interval) VALUES
    ('33333333-0000-0000-0000-000000000001', '11111111-1111-1111-1111-111111111111', 'Starter', 'For small teams', 19.00, 'USD', 'MONTHLY'),
    ('33333333-0000-0000-0000-000000000002', '11111111-1111-1111-1111-111111111111', 'Pro', 'For growing teams', 49.00, 'USD', 'MONTHLY'),
    ('33333333-0000-0000-0000-000000000003', '11111111-1111-1111-1111-111111111111', 'Pro Yearly', 'Pro, billed yearly', 490.00, 'USD', 'YEARLY');

INSERT INTO subscriptions (id, tenant_id, customer_id, plan_id, status, start_date, next_billing_date) VALUES
    ('44444444-0000-0000-0000-000000000001', '11111111-1111-1111-1111-111111111111', '22222222-0000-0000-0000-000000000001', '33333333-0000-0000-0000-000000000002', 'ACTIVE', CURRENT_DATE - 60, CURRENT_DATE - 1),
    ('44444444-0000-0000-0000-000000000002', '11111111-1111-1111-1111-111111111111', '22222222-0000-0000-0000-000000000002', '33333333-0000-0000-0000-000000000001', 'ACTIVE', CURRENT_DATE - 20, CURRENT_DATE + 10);

-- Paid invoice: 49.00 + 12% tax
INSERT INTO invoices (id, tenant_id, customer_id, subscription_id, invoice_number, status, issue_date, due_date, currency, subtotal, discount_total, tax_total, total) VALUES
    ('55555555-0000-0000-0000-000000000001', '11111111-1111-1111-1111-111111111111', '22222222-0000-0000-0000-000000000001', '44444444-0000-0000-0000-000000000001', 'INV-2026-0001', 'PAID', CURRENT_DATE - 40, CURRENT_DATE - 26, 'USD', 49.00, 0.00, 5.88, 54.88);
INSERT INTO invoice_lines (id, invoice_id, description, quantity, unit_price, line_total) VALUES
    ('66666666-0000-0000-0000-000000000001', '55555555-0000-0000-0000-000000000001', 'Pro plan - monthly', 1, 49.00, 49.00);
INSERT INTO payments (id, tenant_id, invoice_id, amount, currency, status, provider, provider_reference, paid_at) VALUES
    ('77777777-0000-0000-0000-000000000001', '11111111-1111-1111-1111-111111111111', '55555555-0000-0000-0000-000000000001', 54.88, 'USD', 'SUCCEEDED', 'STRIPE', 'pi_demo_0001', now() - interval '30 days');

-- Sent invoice: 19.00 + 2 x 50.00, 12% tax
INSERT INTO invoices (id, tenant_id, customer_id, subscription_id, invoice_number, status, issue_date, due_date, currency, subtotal, discount_total, tax_total, total) VALUES
    ('55555555-0000-0000-0000-000000000002', '11111111-1111-1111-1111-111111111111', '22222222-0000-0000-0000-000000000002', '44444444-0000-0000-0000-000000000002', 'INV-2026-0002', 'SENT', CURRENT_DATE - 5, CURRENT_DATE + 9, 'USD', 119.00, 0.00, 14.28, 133.28);
INSERT INTO invoice_lines (id, invoice_id, description, quantity, unit_price, line_total) VALUES
    ('66666666-0000-0000-0000-000000000002', '55555555-0000-0000-0000-000000000002', 'Starter plan - monthly', 1, 19.00, 19.00),
    ('66666666-0000-0000-0000-000000000003', '55555555-0000-0000-0000-000000000002', 'Onboarding session', 2, 50.00, 100.00);

-- Overdue invoice: 10 x 30.00, 30.00 discount, 12% tax on the discounted amount
INSERT INTO invoices (id, tenant_id, customer_id, subscription_id, invoice_number, status, issue_date, due_date, currency, subtotal, discount_total, tax_total, total) VALUES
    ('55555555-0000-0000-0000-000000000003', '11111111-1111-1111-1111-111111111111', '22222222-0000-0000-0000-000000000003', NULL, 'INV-2026-0003', 'OVERDUE', CURRENT_DATE - 45, CURRENT_DATE - 15, 'USD', 300.00, 30.00, 32.40, 302.40);
INSERT INTO invoice_lines (id, invoice_id, description, quantity, unit_price, line_total) VALUES
    ('66666666-0000-0000-0000-000000000004', '55555555-0000-0000-0000-000000000003', 'Website maintenance (hours)', 10, 30.00, 300.00);
