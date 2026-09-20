package com.lending.app.loan.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lending.app.loan.domain.LoanFee;

public interface LoanFeeRepository extends JpaRepository<LoanFee, UUID> {

  @Query("""
          SELECT f
          FROM LoanFee f
          WHERE f.loan.id = :loanId
            AND f.amountPaid < f.amount
          ORDER BY f.appliedDate ASC, f.createdAt ASC
      """)
  List<LoanFee> findOutstandingFees(@Param("loanId") UUID loanId);

  boolean existsByReference(String reference);
}
