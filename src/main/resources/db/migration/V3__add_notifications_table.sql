CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    loan_id UUID,
    notification_type VARCHAR(50) NOT NULL,
    channel VARCHAR(30) NOT NULL,
    recipient VARCHAR(255) NOT NULL,
    subject VARCHAR(255),
    message TEXT NOT NULL,
    status VARCHAR(30) NOT NULL,
    provider VARCHAR(100),
    provider_reference VARCHAR(255),
    sent_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_notifications_customer
        FOREIGN KEY (customer_id)
        REFERENCES customers (id),
    CONSTRAINT fk_notifications_loan
        FOREIGN KEY (loan_id)
        REFERENCES loans (id),
    CONSTRAINT chk_notifications_channel
        CHECK (
            channel IN (
                'SMS',
                'EMAIL',
                'PUSH'
            )
        ),
    CONSTRAINT chk_notifications_status
        CHECK (
            status IN (
                'PENDING',
                'SENT',
                'FAILED'
            )
        )
);

CREATE INDEX idx_notifications_customer_id
    ON notifications (customer_id);

CREATE INDEX idx_notifications_loan_id
    ON notifications (loan_id);

CREATE INDEX idx_notifications_status
    ON notifications (status);

CREATE INDEX idx_notifications_created_at
    ON notifications (created_at);