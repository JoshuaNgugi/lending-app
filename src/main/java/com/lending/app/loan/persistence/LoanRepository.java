package com.lending.app.loan.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import com.lending.app.loan.domain.Loan;

public interface LoanRepository extends JpaRepository<Loan, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM Loan l WHERE l.id = :loanId")
    Optional<Loan> findByIdForUpdate(@Param("loanId") UUID loanId);

    /**
     * Finds active loans that have at least one installment that is either
     * pending, partially paid, or overdue. Overdue loans must remain eligible
     * for later late-fee triggers on subsequent sweeps.
     *
     * @return a list of active loans with outstanding installments
     */
    @Query("""
            SELECT DISTINCT l FROM Loan l
            JOIN l.repaymentSchedule rs
            JOIN Installment i ON i.schedule = rs
            WHERE l.status IN (
                com.lending.app.loan.domain.LoanStatus.OPEN,
                com.lending.app.loan.domain.LoanStatus.OVERDUE)
            AND i.status IN (
                com.lending.app.repayment_schedule.domain.InstallmentStatus.PENDING,
                com.lending.app.repayment_schedule.domain.InstallmentStatus.PARTIALLY_PAID,
                com.lending.app.repayment_schedule.domain.InstallmentStatus.OVERDUE)
            """)
    List<Loan> findActiveLoansWithOutstandingInstallments();

    @Query("""
            SELECT COALESCE(SUM(i.principalDue - i.principalPaid), 0)
            FROM Installment i
            WHERE i.schedule.loan.customer.id = :customerId
            AND i.schedule.loan.status IN (
                com.lending.app.loan.domain.LoanStatus.OPEN,
                com.lending.app.loan.domain.LoanStatus.OVERDUE)
            """)
    BigDecimal calculateOutstandingExposure(@Param("customerId") UUID customerId);
}
