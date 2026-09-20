package com.lending.app;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import com.lending.app.customer.domain.Customer;
import com.lending.app.customer.domain.CustomerSegment;
import com.lending.app.notification.channel.EmailNotificationSender;
import com.lending.app.notification.channel.PushNotificationSender;
import com.lending.app.notification.channel.SmsNotificationSender;

@ExtendWith(OutputCaptureExtension.class)
class NotificationSenderTest {

    private static final String SUBJECT = "Payment due";
    private static final String MESSAGE = "Your payment is due tomorrow.";

    private final Customer customer = new Customer(
            "Maimuna",
            "Maksuudi",
            "maimuna@email.com",
            "254717000002",
            CustomerSegment.BUSINESS);

    @Test
    void emailSenderLogsNotification(CapturedOutput output) {
        new EmailNotificationSender().send(customer, SUBJECT, MESSAGE);

        assertTrue(output.getOut().contains(
                "Notification sent via EMAIL to maimuna@email.com: subject='Payment due', message='Your payment is due tomorrow.'"));
    }

    @Test
    void smsSenderLogsNotification(CapturedOutput output) {
        new SmsNotificationSender().send(customer, SUBJECT, MESSAGE);

        assertTrue(output.getOut().contains(
                "Notification sent via SMS to 254717000002: subject='Payment due', message='Your payment is due tomorrow.'"));
    }

    @Test
    void pushSenderLogsNotification(CapturedOutput output) {
        new PushNotificationSender().send(customer, SUBJECT, MESSAGE);

        assertTrue(output.getOut().contains(
                "Notification sent via PUSH to customer Maimuna Maksuudi: subject='Payment due', message='Your payment is due tomorrow.'"));
    }
}
