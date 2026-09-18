package com.lending.app.notification.channel;

import org.springframework.stereotype.Component;

import com.lending.app.customer.domain.Customer;

@Component
public class EmailNotificationSender implements NotificationSender {

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public void send(Customer customer, String subject, String message) {

    }
}