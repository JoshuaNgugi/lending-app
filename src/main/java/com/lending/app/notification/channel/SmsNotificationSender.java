package com.lending.app.notification.channel;

import org.springframework.stereotype.Component;

import com.lending.app.customer.domain.Customer;

@Component
public class SmsNotificationSender implements NotificationSender {

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.SMS;
    }

    @Override
    public void send(Customer customer, String subject, String message) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'send'");
    }
}
