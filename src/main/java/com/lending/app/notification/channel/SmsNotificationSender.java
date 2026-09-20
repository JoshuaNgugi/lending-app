package com.lending.app.notification.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.lending.app.customer.domain.Customer;

@Component
public class SmsNotificationSender implements NotificationSender {

    private static final Logger logger = LoggerFactory.getLogger(SmsNotificationSender.class);

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.SMS;
    }

    @Override
    public void send(Customer customer, String subject, String message) {
        logger.info("Notification sent via SMS to {}: subject='{}', message='{}'",
            customer.getPhoneNumber(), subject, message);
    }
}
