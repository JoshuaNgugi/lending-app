CREATE TABLE repayment_schedules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    loan_id UUID NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ,
    CONSTRAINT fk_repayment_schedule_loan
        FOREIGN KEY (loan_id)
        REFERENCES loans(id),
    CONSTRAINT chk_repayment_schedule_status
        CHECK (
            status IN (
                'ACTIVE',
                'COMPLETED',
                'CANCELLED'
            )
        )
);

CREATE TABLE installments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    schedule_id UUID NOT NULL,
    installment_number INTEGER NOT NULL,
    due_date DATE NOT NULL,
    principal_due NUMERIC(19, 2) NOT NULL,
    principal_paid NUMERIC(19, 2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT fk_installment_schedule
        FOREIGN KEY (schedule_id)
        REFERENCES repayment_schedules(id),
    CONSTRAINT uq_installment_schedule_number
        UNIQUE (schedule_id, installment_number),
    CONSTRAINT chk_installment_number
        CHECK (installment_number > 0),
    CONSTRAINT chk_installment_principal
        CHECK (principal_due > 0),
    CONSTRAINT chk_installment_principal_paid
        CHECK (principal_paid >= 0),
    CONSTRAINT chk_installment_status
        CHECK (
            status IN (
                'PENDING',
                'PARTIALLY_PAID',
                'PAID',
                'OVERDUE'
            )
        )
);

CREATE INDEX idx_installment_schedule
    ON installments(schedule_id);

CREATE INDEX idx_installment_due_date
    ON installments(due_date);

CREATE INDEX idx_installment_status
    ON installments(status);