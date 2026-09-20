package com.lending.app.notification.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.lending.app.customer.domain.Customer;

@Component
public class EmailNotificationSender implements NotificationSender {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationSender.class);

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public void send(Customer customer, String subject, String message) {
        logger.info("Notification sent via EMAIL to {}: subject='{}', message='{}'",
            customer.getEmail(), subject, message);
    }
}