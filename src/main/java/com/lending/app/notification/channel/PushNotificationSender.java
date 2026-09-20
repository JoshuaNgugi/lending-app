package com.lending.app.notification.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.lending.app.customer.domain.Customer;

@Component
public class PushNotificationSender implements NotificationSender {

    private static final Logger logger = LoggerFactory.getLogger(PushNotificationSender.class);

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.PUSH;
    }

    @Override
    public void send(Customer customer, String subject, String message) {
        logger.info("Notification sent via PUSH to customer {} {}: subject='{}', message='{}'",
            customer.getFirstName(), customer.getLastName(), subject, message);
    }
}
