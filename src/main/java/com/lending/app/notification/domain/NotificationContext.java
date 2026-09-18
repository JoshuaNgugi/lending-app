package com.lending.app.notification.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.lending.app.customer.domain.Customer;

public record NotificationContext(
        Customer customer,
        UUID loanId,
        BigDecimal outstandingAmount,
        LocalDate dueDate) {
}
