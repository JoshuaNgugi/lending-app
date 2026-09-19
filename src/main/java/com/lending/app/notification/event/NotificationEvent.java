package com.lending.app.notification.event;

import java.util.Map;
import java.util.UUID;

public record NotificationEvent(
                NotificationEventType eventType,
                UUID loanId,
                UUID customerId,
                Map<String, Object> variables) {
}
