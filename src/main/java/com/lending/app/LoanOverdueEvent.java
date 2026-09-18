package com.lending.app;

import java.util.UUID;

public record LoanOverdueEvent(
        UUID loanId,
        UUID customerId) {

}
