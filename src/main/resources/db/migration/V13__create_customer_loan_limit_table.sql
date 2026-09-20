CREATE TABLE customer_loan_limits (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL,
    limit_amount NUMERIC(19, 2) NOT NULL CHECK (limit_amount > 0),
    currency VARCHAR(3) NOT NULL,
    effective_from DATE NOT NULL,
    effective_to DATE,
    reason VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT fk_customer_loan_limit_customer
        FOREIGN KEY (customer_id)
        REFERENCES customers(id),
    CONSTRAINT chk_customer_loan_limit_status
        CHECK (status IN ('ACTIVE', 'EXPIRED', 'SUSPENDED')),
    CONSTRAINT chk_customer_loan_limit_dates
        CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE INDEX idx_customer_loan_limits_customer_status
    ON customer_loan_limits (customer_id, status, effective_from DESC);