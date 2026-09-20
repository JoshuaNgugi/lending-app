package com.lending.app.customer.application;

import java.math.BigDecimal;

import com.lending.app.customer.domain.CustomerLoanLimit;

public record CustomerLoanLimitDetails(
        CustomerLoanLimit limit,
        BigDecimal availableAmount) {
}
