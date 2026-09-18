INSERT INTO notification_templates (
    id,
    code,
    subject,
    body,
    enabled
)
VALUES (
    gen_random_uuid(),
    'loan-overdue-sms',
    'Loan overdue',
    'Hello {{firstName}}, your loan {{loanId}} is overdue. Please make your repayment.',
    TRUE
);

INSERT INTO notification_rules (
    id,
    event_type,
    customer_segment,
    channel,
    template_code,
    enabled
)
VALUES (
    gen_random_uuid(),
    'LOAN_OVERDUE',
    NULL,
    'SMS',
    'loan-overdue-sms',
    TRUE
);