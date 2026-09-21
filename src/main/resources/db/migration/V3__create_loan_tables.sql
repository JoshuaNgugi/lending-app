CREATE TABLE loans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL,
    product_id UUID NOT NULL,
    principal NUMERIC(19, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    origination_date DATE,
    disbursement_date DATE,
    maturity_date DATE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ,
    CONSTRAINT fk_loan_customer
        FOREIGN KEY (customer_id)
        REFERENCES customers(id),
    CONSTRAINT fk_loan_product
        FOREIGN KEY (product_id)
        REFERENCES loan_products(id),
    CONSTRAINT chk_loan_principal
        CHECK (principal > 0),
    CONSTRAINT chk_loan_status
        CHECK (
            status IN (
                'CREATED',
                'OPEN',
                'OVERDUE',
                'CLOSED',
                'CANCELLED',
                'WRITTEN_OFF'
            )
        )
);

CREATE INDEX idx_loan_customer
    ON loans(customer_id);

CREATE INDEX idx_loan_product
    ON loans(product_id);

CREATE INDEX idx_loan_status
    ON loans(status);