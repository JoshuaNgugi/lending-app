package com.lending.app.notification.event;

import java.util.UUID;

public record LoanOverdueEvent(
        UUID loanId,
        UUID customerId) {

}
