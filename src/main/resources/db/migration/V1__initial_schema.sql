CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE customers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone_number VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL,
    segment VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_customer_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'BLOCKED')),
    CONSTRAINT chk_customer_segment
        CHECK (segment IN ('RETAIL', 'SALARIED', 'BUSINESS'))
);

CREATE UNIQUE INDEX uq_customer_email
    ON customers (LOWER(email));

CREATE UNIQUE INDEX uq_customer_phone
    ON customers (phone_number);

CREATE INDEX idx_customer_status
    ON customers (status);

CREATE INDEX idx_customer_segment
    ON customers (segment);
