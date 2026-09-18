ALTER TABLE loan_fees
    ADD CONSTRAINT uq_loan_fee_reference UNIQUE (reference);