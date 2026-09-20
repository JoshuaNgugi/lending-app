package com.lending.app.loan.application;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.lending.app.customer.domain.Customer;
import com.lending.app.customer.domain.CustomerLoanLimit;
import com.lending.app.customer.domain.CustomerLoanLimitStatus;
import com.lending.app.customer.repository.CustomerLoanLimitRepository;
import com.lending.app.loan.repository.LoanRepository;

@Component
public class LoanLimitValidator {

    private final CustomerLoanLimitRepository limitRepository;
    private final LoanRepository loanRepository;

    public LoanLimitValidator(CustomerLoanLimitRepository limitRepository, LoanRepository loanRepository) {
        this.limitRepository = limitRepository;
        this.loanRepository = loanRepository;
    }

    public void validate(Customer customer, BigDecimal requestedAmount) {
        CustomerLoanLimit limit = limitRepository
                .findFirstByCustomerIdAndStatusOrderByEffectiveFromDesc(customer.getId(),
                        CustomerLoanLimitStatus.ACTIVE)
                .orElseThrow(() -> new IllegalStateException("Customer has no active loan limit"));

        BigDecimal outstandingExposure = loanRepository.calculateOutstandingExposure(customer.getId());

        BigDecimal availableLimit = limit.getLimitAmount().subtract(outstandingExposure);

        if (requestedAmount.compareTo(availableLimit) > 0) {
            throw new IllegalArgumentException("Loan amount exceeds available customer limit");
        }
    }
}
