CREATE TABLE customer_notification_preferences (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    event_type VARCHAR(30) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_customer_notification_preference_customer
        FOREIGN KEY (customer_id)
        REFERENCES customers(id),

    CONSTRAINT uq_customer_notification_preference
        UNIQUE (customer_id, event_type, channel)
);

CREATE INDEX idx_customer_notification_preferences_customer
    ON customer_notification_preferences(customer_id);