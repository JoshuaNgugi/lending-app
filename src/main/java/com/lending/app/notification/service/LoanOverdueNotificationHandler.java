package com.lending.app.notification.service;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.lending.app.customer.domain.Customer;
import com.lending.app.customer.repository.CustomerRepository;
import com.lending.app.notification.channel.NotificationChannel;
import com.lending.app.notification.channel.NotificationSender;
import com.lending.app.notification.channel.NotificationSenderFactory;
import com.lending.app.notification.event.LoanOverdueEvent;
import com.lending.app.shared.exception.ResourceNotFoundException;

@Component
public class LoanOverdueNotificationHandler {

    private final CustomerRepository customerRepository;
    private final NotificationSenderFactory senderFactory;

    public LoanOverdueNotificationHandler(
            CustomerRepository customerRepository,
            NotificationSenderFactory senderFactory) {

        this.customerRepository = customerRepository;
        this.senderFactory = senderFactory;
    }

    @EventListener
    public void handle(LoanOverdueEvent event) {

        Customer customer = customerRepository
                .findById(event.customerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found: " + event.customerId()));

        NotificationSender sender = senderFactory.getSender(NotificationChannel.SMS);

        sender.send(
                customer,
                "Loan overdue",
                "Your loan is overdue. Please make your repayment.");
    }
}
