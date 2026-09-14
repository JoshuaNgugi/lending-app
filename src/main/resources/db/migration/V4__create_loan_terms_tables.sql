CREATE TABLE loan_terms (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    loan_id UUID NOT NULL UNIQUE,
    tenure_value INTEGER NOT NULL,
    tenure_unit VARCHAR(10) NOT NULL,
    structure VARCHAR(20) NOT NULL,
    billing_mode VARCHAR(20) NOT NULL,
    billing_day INTEGER,
    grace_period_days INTEGER NOT NULL,
    CONSTRAINT fk_loan_terms_loan
        FOREIGN KEY (loan_id)
        REFERENCES loans(id),
    CONSTRAINT chk_loan_terms_tenure
        CHECK (tenure_value > 0),
    CONSTRAINT chk_loan_terms_billing_day
        CHECK (
            billing_day IS NULL
            OR billing_day BETWEEN 1 AND 28
        ),
    CONSTRAINT chk_loan_terms_grace_period
        CHECK (grace_period_days >= 0)
);


CREATE TABLE loan_term_fees (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    loan_terms_id UUID NOT NULL,
    fee_type VARCHAR(20) NOT NULL,
    calculation_type VARCHAR(20) NOT NULL,
    value NUMERIC(19, 6) NOT NULL,
    application_timing VARCHAR(30) NOT NULL,
    trigger_days INTEGER,
    CONSTRAINT fk_loan_term_fee_terms
        FOREIGN KEY (loan_terms_id)
        REFERENCES loan_terms(id),
    CONSTRAINT chk_loan_term_fee_value
        CHECK (value > 0),
    CONSTRAINT chk_loan_term_fee_trigger_days
        CHECK (
            trigger_days IS NULL
            OR trigger_days >= 0
        )
);

CREATE INDEX idx_loan_term_fee_terms
    ON loan_term_fees(loan_terms_id);

CREATE TABLE loan_fees (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    loan_id UUID NOT NULL,
    fee_type VARCHAR(20) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    applied_date DATE NOT NULL,
    reference_date DATE,
    reference VARCHAR(100),
    reason VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_loan_fee_loan
        FOREIGN KEY (loan_id)
        REFERENCES loans(id),
    CONSTRAINT chk_loan_fee_amount
        CHECK (amount > 0)
);

CREATE INDEX idx_loan_fee_loan
    ON loan_fees(loan_id);