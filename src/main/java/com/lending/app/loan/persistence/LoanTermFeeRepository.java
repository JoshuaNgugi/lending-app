package com.lending.app.loan.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lending.app.loan.domain.LoanTermFee;

public interface LoanTermFeeRepository extends JpaRepository<LoanTermFee, UUID> {

    List<LoanTermFee> findByLoanTermsId(UUID loanTermsId);
}
