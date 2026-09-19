package com.lending.app.notification.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.lending.app.loan.domain.Loan;
import com.lending.app.notification.event.NotificationEvent;
import com.lending.app.notification.event.NotificationEventType;
import com.lending.app.repayment_schedule.domain.Installment;
import com.lending.app.repayment_schedule.repository.InstallmentRepository;

/**
 * Service responsible for sending payment due notifications to customers.
 */
@Service
public class PaymentDueNotificationService {

    private final InstallmentRepository installmentRepository;
    private final ApplicationEventPublisher eventPublisher;

    public PaymentDueNotificationService(InstallmentRepository installmentRepository,
            ApplicationEventPublisher eventPublisher) {

        this.installmentRepository = installmentRepository;
        this.eventPublisher = eventPublisher;
    }

    public void execute(LocalDate today, int daysBeforeDue) {
        LocalDate dueDate = today.plusDays(daysBeforeDue);

        List<Installment> installments = installmentRepository.findOutstandingInstallmentDueOn(dueDate);

        for (Installment installment : installments) {

            Loan loan = installment.getSchedule().getLoan();

            Map<String, Object> variables = Map.of(
                    "amount", installment.getOutstandingPrincipal(),
                    "dueDate", installment.getDueDate(),
                    "installmentNumber", installment.getInstallmentNumber());

            eventPublisher.publishEvent(
                    new NotificationEvent(NotificationEventType.PAYMENT_DUE, loan.getId(), loan.getCustomer().getId(),
                            variables));
        }
    }
}
