CREATE TABLE notification_templates (
    id UUID PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    subject VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE notification_rules (
    id UUID PRIMARY KEY,
    event_type VARCHAR(30) NOT NULL,
    customer_segment VARCHAR(20),
    channel VARCHAR(20) NOT NULL,
    template_code VARCHAR(100) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_notification_rule_template
        FOREIGN KEY (template_code)
        REFERENCES notification_templates(code)
);

CREATE INDEX idx_notification_rules_event_enabled
    ON notification_rules(event_type, enabled);