INSERT INTO notification_templates (
    id, code, subject, body, enabled
)
VALUES (
    gen_random_uuid(),
    'payment-due',
    'Payment due reminder',
    'Hello {{firstName}}, your loan {{productName}} has a payment of KES {{amount}} due on {{dueDate}}.'
);

INSERT INTO notification_rules (
    id, event_type, customer_segment, channel, template_code, enabled
)
VALUES (
    gen_random_uuid(), 'PAYMENT_DUE', NULL, 'SMS', 'payment-due', TRUE
);