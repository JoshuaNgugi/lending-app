CREATE TABLE repayments (
    id UUID PRIMARY KEY,
    loan_id UUID NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    payment_reference VARCHAR(100) NOT NULL UNIQUE,
    payment_method VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    paid_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_repayments_loan
        FOREIGN KEY (loan_id)
        REFERENCES loans (id),
    CONSTRAINT chk_repayments_amount
        CHECK (amount > 0),
    CONSTRAINT chk_repayments_payment_method
        CHECK (
            payment_method IN (
                'CASH',
                'BANK_TRANSFER',
                'MOBILE_MONEY',
                'CARD'
            )
        ),
    CONSTRAINT chk_repayments_status
        CHECK (
            status IN (
                'PENDING',
                'COMPLETED',
                'FAILED',
                'REVERSED'
            )
        )
);

CREATE INDEX idx_repayments_loan_id
    ON repayments (loan_id);

CREATE INDEX idx_repayments_status
    ON repayments (status);

CREATE INDEX idx_repayments_paid_at
    ON repayments (paid_at);