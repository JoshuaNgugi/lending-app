package com.lending.app.notification.application;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.lending.app.notification.service.PaymentDueNotificationService;

@Component
public class PaymentDueNotificationScheduler {

    private final PaymentDueNotificationService paymentDueNotificationService;

    @Value("${loan.payment-due-reminder.days-before}")
    private int daysBeforeDue;

    public PaymentDueNotificationScheduler(PaymentDueNotificationService paymentDueNotificationService) {
        this.paymentDueNotificationService = paymentDueNotificationService;
    }

    @Scheduled(cron = "${loan.payment-due-reminder.cron}")
    public void sweep() {
        paymentDueNotificationService.execute(LocalDate.now(), daysBeforeDue);
    }
}
