package com.lending.app.loan.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.lending.app.loan.domain.Loan;

public interface LoanRepository extends JpaRepository<Loan, UUID> {

    /**
     * Finds all open loans that have at least one installment that is either
     * pending or partially paid.
     *
     * @return a list of open loans with outstanding installments
     */
    @Query("""
            SELECT DISTINCT l FROM Loan l
            JOIN l.repaymentSchedule rs
            JOIN Installment i ON i.schedule = rs
            WHERE l.status = com.lending.app.loan.domain.LoanStatus.OPEN
            AND i.status IN (
                com.lending.app.repayment_schedule.domain.InstallmentStatus.PENDING,
                com.lending.app.repayment_schedule.domain.InstallmentStatus.PARTIALLY_PAID)
            """)
    List<Loan> findOpenLoansWithOutstandingInstallments();
}
