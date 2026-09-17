ALTER TABLE loan_fees
    ADD COLUMN amount_paid NUMERIC(19, 2) NOT NULL DEFAULT 0;

ALTER TABLE loan_fees
    ADD CONSTRAINT chk_loan_fee_amount_paid
    CHECK (amount_paid >= 0 and amount_paid <= amount);

CREATE TABLE repayments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    loan_id UUID NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    payment_date DATE NOT NULL,
    reference VARCHAR(100) NOT NULL,
    channel VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_repayment_loan
        FOREIGN KEY (loan_id)
        REFERENCES loans(id),

    CONSTRAINT uq_repayment_reference
        UNIQUE (reference),

    CONSTRAINT chk_repayment_amount
        CHECK (amount > 0)
);

CREATE TABLE repayment_allocations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    repayment_id UUID NOT NULL,
    loan_fee_id UUID,
    installment_id UUID,
    amount NUMERIC(19, 2) NOT NULL,

    CONSTRAINT fk_repayment_allocation_repayment
        FOREIGN KEY (repayment_id)
        REFERENCES repayments(id),

    CONSTRAINT fk_repayment_allocation_loan_fee
        FOREIGN KEY (loan_fee_id)
        REFERENCES loan_fees(id),

    CONSTRAINT fk_repayment_allocation_installment
        FOREIGN KEY (installment_id)
        REFERENCES installments(id),

    CONSTRAINT chk_repayment_allocation_amount
        CHECK (amount > 0),
    
    CONSTRAINT chk_repayment_allocation_target
        CHECK (
            (loan_fee_id IS NOT NULL AND installment_id IS NULL)
            OR
            (loan_fee_id IS NULL AND installment_id is NOT NULL)
        )
);

CREATE INDEX idx_repayment_loan
    ON repayments(loan_id);

CREATE INDEX idx_repayment_allocation_repayment
    ON repayment_allocations(repayment_id);

CREATE INDEX idx_repayment_allocation_loan_fee
    ON repayment_allocations(loan_fee_id);

CREATE INDEX idx_repayment_allocation_installment
    ON repayment_allocations(installment_id);