package com.lending.app.loan.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.lending.app.loan.domain.LoanStatus;

public record LoanResponse(
        UUID id,
        UUID customerId,
        UUID productId,
        BigDecimal principal,
        LoanStatus status,
        LocalDate originationDate,
        LocalDate disbursementDate,
        LocalDate maturityDate) {

}
