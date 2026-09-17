package com.lending.app.repayment.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record RepaymentResponse(
                UUID id,
                UUID loanId,
                BigDecimal amount,
                LocalDate paymentDate,
                String reference,
                String channel,
                List<RepaymentAllocationResponse> allocations) {

}
