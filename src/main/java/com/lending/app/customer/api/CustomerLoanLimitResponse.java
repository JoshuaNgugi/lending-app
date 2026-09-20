package com.lending.app.customer.api;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CustomerLoanLimitResponse(
        BigDecimal limitAmount,
        String currency,
        BigDecimal availableAmount,
        String reason,
        LocalDate effectiveFrom,
        String status) {
}