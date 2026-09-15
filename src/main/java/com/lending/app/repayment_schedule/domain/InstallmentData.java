package com.lending.app.repayment_schedule.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InstallmentData(
        int installmentNumber, LocalDate duDate, BigDecimal principalDue) {

}
