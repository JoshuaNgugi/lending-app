package com.lending.app.customer.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lending.app.customer.domain.CustomerLoanLimit;
import com.lending.app.customer.domain.CustomerLoanLimitStatus;

public interface CustomerLoanLimitRepository
                extends JpaRepository<CustomerLoanLimit, UUID> {

        Optional<CustomerLoanLimit> findFirstByCustomerIdAndStatusOrderByEffectiveFromDesc(
                        UUID customerId,
                        CustomerLoanLimitStatus status);

        List<CustomerLoanLimit> findByCustomerIdOrderByEffectiveFromDesc(
                        UUID customerId);
}
