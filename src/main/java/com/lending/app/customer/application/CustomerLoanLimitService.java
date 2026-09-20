package com.lending.app.customer.application;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lending.app.customer.domain.Customer;
import com.lending.app.customer.domain.CustomerLoanLimit;
import com.lending.app.customer.domain.CustomerLoanLimitStatus;
import com.lending.app.customer.persistence.CustomerLoanLimitRepository;
import com.lending.app.customer.persistence.CustomerRepository;
import com.lending.app.loan.repository.LoanRepository;
import com.lending.app.shared.exception.ResourceNotFoundException;

@Service
@Transactional
public class CustomerLoanLimitService {

        private final CustomerRepository customerRepository;
        private final CustomerLoanLimitRepository limitRepository;
        private final LoanRepository loanRepository;

        public CustomerLoanLimitService(
                        CustomerRepository customerRepository,
                        CustomerLoanLimitRepository limitRepository,
                        LoanRepository loanRepository) {

                this.customerRepository = customerRepository;
                this.limitRepository = limitRepository;
                this.loanRepository = loanRepository;
        }

        public CustomerLoanLimitDetails setLimit(UUID customerId, BigDecimal limitAmount,
                        String currency, String reason) {

                Customer customer = customerRepository.findByIdForUpdate(customerId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Customer not found: " + customerId));

                CustomerLoanLimit currentLimit = limitRepository
                                .findFirstByCustomerIdAndStatusOrderByEffectiveFromDesc(
                                                customerId,
                                                CustomerLoanLimitStatus.ACTIVE)
                                .orElse(null);

                if (currentLimit != null) {
                        currentLimit.expire();
                        limitRepository.save(currentLimit);
                }

                CustomerLoanLimit newLimit = new CustomerLoanLimit(
                                customer,
                                limitAmount,
                                currency,
                                LocalDate.now(),
                                reason);

                CustomerLoanLimit savedLimit = limitRepository.save(newLimit);

                BigDecimal outstandingExposure = loanRepository.calculateOutstandingExposure(customerId);

                BigDecimal availableAmount = savedLimit.getLimitAmount()
                                .subtract(outstandingExposure)
                                .max(BigDecimal.ZERO);

                return new CustomerLoanLimitDetails(savedLimit, availableAmount);
        }

        @Transactional(readOnly = true)
        public CustomerLoanLimitDetails getLimit(UUID customerId) {
                customerRepository.findById(customerId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Customer not found: " + customerId));

                CustomerLoanLimit limit = limitRepository
                                .findFirstByCustomerIdAndStatusOrderByEffectiveFromDesc(
                                                customerId,
                                                CustomerLoanLimitStatus.ACTIVE)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Customer has no active loan limit: " + customerId));

                BigDecimal outstandingExposure = loanRepository.calculateOutstandingExposure(customerId);
                BigDecimal availableAmount = limit.getLimitAmount().subtract(outstandingExposure)
                                .max(BigDecimal.ZERO);

                return new CustomerLoanLimitDetails(limit, availableAmount);
        }

}
