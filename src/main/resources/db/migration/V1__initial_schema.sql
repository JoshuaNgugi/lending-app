CREATE TABLE customers (
    id UUID PRIMARY KEY,
    first_name VARCHAR(20) NOT NULL,
    last_name VARCHAR(20) NOT NULL,
    phone_number VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_customers_phone_number ON customers (phone_number);

CREATE TABLE loan_products (
    id UUID PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(500),
    currency VARCHAR(3) NOT NULL,
    min_amount NUMERIC(19, 2) NOT NULL,
    max_amount NUMERIC(19, 2) NOT NULL,
    interest_rate NUMERIC(10, 4) NOT NULL,
    term_in_days INTEGER NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,

    CONSTRAINT chk_loan_products_amounts
        CHECK (min_amount > 0 AND max_amount >= min_amount),

    CONSTRAINT chk_loan_products_interest_rate
        CHECK (interest_rate >= 0),

    CONSTRAINT chk_loan_products_term
        CHECK (term_in_days > 0),

    CONSTRAINT chk_loan_products_status
        CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE TABLE loans (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    loan_product_id UUID NOT NULL,
    principal_amount NUMERIC(19, 2) NOT NULL,
    interest_rate NUMERIC(10, 4) NOT NULL,
    interest_amount NUMERIC(19, 2) NOT NULL,
    total_amount NUMERIC(19, 2) NOT NULL,
    outstanding_amount NUMERIC(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(30) NOT NULL,
    application_date TIMESTAMP WITH TIME ZONE NOT NULL,
    disbursed_at TIMESTAMP WITH TIME ZONE,
    due_date TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_loans_customer
        FOREIGN KEY (customer_id)
        REFERENCES customers (id),
    CONSTRAINT fk_loans_product
        FOREIGN KEY (loan_product_id)
        REFERENCES loan_products (id),
    CONSTRAINT chk_loans_principal
        CHECK (principal_amount > 0),
    CONSTRAINT chk_loans_interest_rate
        CHECK (interest_rate >= 0),
    CONSTRAINT chk_loans_interest_amount
        CHECK (interest_amount >= 0),
    CONSTRAINT chk_loans_total_amount
        CHECK (total_amount >= principal_amount),
    CONSTRAINT chk_loans_outstanding_amount
        CHECK (outstanding_amount >= 0),
    CONSTRAINT chk_loans_status
        CHECK (
            status IN (
                'PENDING',
                'APPROVED',
                'DISBURSED',
                'PARTIALLY_PAID',
                'PAID',
                'REJECTED',
                'DEFAULTED',
                'CANCELLED'
            )
        )
);

CREATE INDEX idx_loans_customer_id
    ON loans (customer_id);

CREATE INDEX idx_loans_product_id
    ON loans (loan_product_id);

CREATE INDEX idx_loans_status
    ON loans (status);

CREATE INDEX idx_loans_due_date
    ON loans (due_date);