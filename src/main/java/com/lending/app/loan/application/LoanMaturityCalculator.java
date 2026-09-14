package com.lending.app.loan.application;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import com.lending.app.product.domain.TenureUnit;

@Component
public class LoanMaturityCalculator {

    public LocalDate calculate(LocalDate disbursementDate, Integer tenureValue, TenureUnit tenureUnit) {
        return switch (tenureUnit) {
            case DAYS -> disbursementDate.plusDays(tenureValue);
            case MONTHS -> disbursementDate.plusMonths(tenureValue);
        };
    }
}
