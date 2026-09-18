package com.lending.app.notification.channel;

import com.lending.app.customer.domain.Customer;

/**
 * Strategy interface for sending notifications through different channels.
 */
public interface NotificationSender {

    NotificationChannel channel();

    void send(Customer customer, String subject, String message);
}
