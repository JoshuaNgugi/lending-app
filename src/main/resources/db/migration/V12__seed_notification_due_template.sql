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

INSERT INTO notification_templates (
    id, code, subject, body, enabled
)
VALUES (
    gen_random_uuid(),
    'loan-created',
    'Loan created',
    'Hello {{firstName}}, your loan {{productName}} of {{amount}} has been created successfully.',
    TRUE
);

INSERT INTO notification_rules (
    id, event_type, customer_segment, channel, template_code, enabled
)
VALUES (
    gen_random_uuid(), 'LOAN_CREATED', NULL, 'SMS', 'loan-created', TRUE
);

INSERT INTO notification_templates (
    id, code, subject, body, enabled
)
VALUES (
    gen_random_uuid(),
    'payment-received',
    'Payment received',
    'Hello {{firstName}}, we have received your payment of {{amount}} for loan {{productName}}. Reference: {{reference}}.',
    TRUE
);

INSERT INTO notification_rules (
    id, event_type, customer_segment, channel, template_code, enabled
)
VALUES (
    gen_random_uuid(), 'PAYMENT_RECEIVED', NULL, 'SMS', 'payment-received', TRUE
);

INSERT INTO notification_templates (
    id, code, subject, body, enabled
)
VALUES (
    gen_random_uuid(),
    'loan-overdue',
    'Loan overdue',
    'Hello {{firstName}}, your loan {{productName}} is overdue. Please make your repayment as soon as possible.',
    TRUE
);

INSERT INTO notification_rules (
    id, event_type, customer_segment, channel, template_code, enabled
)
VALUES (
    gen_random_uuid(), 'LOAN_OVERDUE', NULL, 'SMS', 'loan-overdue', TRUE
);