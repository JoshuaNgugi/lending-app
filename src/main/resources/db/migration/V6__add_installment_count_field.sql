ALTER TABLE loan_products
    ADD COLUMN installment_count INTEGER;

ALTER TABLE loan_terms
    ADD COLUMN installment_count INTEGER;
    
ALTER TABLE loan_products
    ADD CONSTRAINT chk_loan_product_installment_count
    CHECK (installment_count IS NULL OR installment_count > 0);

ALTER TABLE loan_terms
    ADD CONSTRAINT chk_loan_terms_installment_count
    CHECK (installment_count IS NULL OR installment_count > 0);