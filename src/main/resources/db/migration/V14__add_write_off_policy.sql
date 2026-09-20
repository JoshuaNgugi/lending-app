ALTER TABLE loan_products
    ADD COLUMN write_off_after_days INTEGER;

ALTER TABLE loan_products
    ADD CONSTRAINT chk_product_write_off_after_days
    CHECK (write_off_after_days IS NULL OR write_off_after_days > 0);

ALTER TABLE loan_terms
    ADD COLUMN write_off_after_days INTEGER;

ALTER TABLE loan_terms
    ADD CONSTRAINT chk_loan_terms_write_off_after_days
    CHECK (write_off_after_days IS NULL OR write_off_after_days > 0);