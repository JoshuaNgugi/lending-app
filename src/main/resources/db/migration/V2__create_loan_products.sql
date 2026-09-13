CREATE TABLE loan_products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(500),
    status VARCHAR(20) NOT NULL,
    tenure_value INTEGER NOT NULL,
    tenure_unit VARCHAR(10) NOT NULL,
    structure VARCHAR(20) NOT NULL,
    billing_mode VARCHAR(20) NOT NULL,
    billing_day INTEGER,
    grace_period_days INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ,
    CONSTRAINT uq_loan_product_code UNIQUE (code),
    CONSTRAINT chk_product_tenure CHECK (tenure_value > 0),
    CONSTRAINT chk_product_billing_day
        CHECK (billing_day IS NULL OR billing_day BETWEEN 1 AND 28),
    CONSTRAINT chk_product_grace_period CHECK (grace_period_days >= 0),
    CONSTRAINT chk_product_status
        CHECK (status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT chk_product_tenure_unit
        CHECK (tenure_unit IN ('DAYS', 'MONTHS')),
    CONSTRAINT chk_product_structure
        CHECK (structure IN ('LUMP_SUM', 'INSTALLMENT')),
    CONSTRAINT chk_product_billing_mode
        CHECK (billing_mode IN ('INDIVIDUAL', 'CONSOLIDATED'))
);

CREATE TABLE product_fees (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL,
    fee_type VARCHAR(20) NOT NULL,
    calculation_type VARCHAR(20) NOT NULL,
    value NUMERIC(19, 6) NOT NULL,
    application_timing VARCHAR(30) NOT NULL,
    trigger_days INTEGER,
    CONSTRAINT fk_product_fee_product
        FOREIGN KEY (product_id)
        REFERENCES loan_products(id)
        ON DELETE CASCADE,
    CONSTRAINT chk_product_fee_value
        CHECK (value > 0),
    CONSTRAINT chk_product_fee_trigger_days
        CHECK (trigger_days IS NULL OR trigger_days >= 0)
);

CREATE INDEX idx_product_fee_product ON product_fees(product_id);