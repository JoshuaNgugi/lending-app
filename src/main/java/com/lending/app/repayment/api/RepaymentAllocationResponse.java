package com.lending.app.repayment.api;

import java.math.BigDecimal;
import java.util.UUID;

public record RepaymentAllocationResponse(
        UUID id,
        UUID loanFeeId,
        UUID installmentId,
        BigDecimal amount) {

}
