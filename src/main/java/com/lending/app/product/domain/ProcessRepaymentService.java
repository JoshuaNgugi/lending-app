package com.lending.app.product.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lending.app.loan.domain.Loan;
import com.lending.app.loan.domain.LoanFee;
import com.lending.app.loan.domain.LoanStatus;
import com.lending.app.loan.repository.LoanFeeRepository;
import com.lending.app.loan.repository.LoanRepository;
import com.lending.app.repayment.domain.Repayment;
import com.lending.app.repayment.domain.RepaymentAllocation;
import com.lending.app.repayment.repayment.RepaymentAllocationRepository;
import com.lending.app.repayment.repayment.RepaymentRepository;
import com.lending.app.repayment_schedule.domain.Installment;
import com.lending.app.repayment_schedule.domain.RepaymentSchedule;
import com.lending.app.repayment_schedule.repository.InstallmentRepository;
import com.lending.app.shared.exception.ResourceNotFoundException;

@Service
@Transactional
public class ProcessRepaymentService {

    private final LoanRepository loanRepository;
    private final RepaymentRepository repaymentRepository;
    private final RepaymentAllocationRepository repaymentAllocationRepository;
    private final LoanFeeRepository loanFeeRepository;
    private final InstallmentRepository installmentRepository;

    public ProcessRepaymentService(
            LoanRepository loanRepository,
            RepaymentRepository repaymentRepository,
            RepaymentAllocationRepository repaymentAllocationRepository,
            LoanFeeRepository loanFeeRepository,
            InstallmentRepository installmentRepository) {
        this.loanRepository = loanRepository;
        this.repaymentRepository = repaymentRepository;
        this.repaymentAllocationRepository = repaymentAllocationRepository;
        this.loanFeeRepository = loanFeeRepository;
        this.installmentRepository = installmentRepository;
    }

    public Repayment execute(UUID loanId, BigDecimal amount, String reference, String channel) {

        validateAmount(amount);

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found: " + loanId));

        validateLoan(loan);

        if (repaymentRepository.existsByReference(reference)) {
            throw new IllegalStateException("Repayment with reference: " + reference + " already exists");
        }

        RepaymentSchedule schedule = loan.getRepaymentSchedule();

        if (schedule == null) {
            throw new IllegalStateException("Repayment schedule does not exist for loan: " + loanId);
        }

        List<LoanFee> outstandingFees = loanFeeRepository.findOutstandingFees(loanId);

        List<Installment> outstandingInstallments = installmentRepository.findOutstandingInstallments(schedule.getId());

        BigDecimal totalOutstanding = calculateTotalOutstanding(outstandingFees, outstandingInstallments);

        if (amount.compareTo(totalOutstanding) > 0) {
            throw new IllegalArgumentException("Repayment amount exceeds total outstanding balance");
        }

        Repayment repayment = new Repayment(loan, amount, LocalDate.now(), reference, channel);

        repayment = repaymentRepository.save(repayment);

        BigDecimal remainingAmount = amount;

        remainingAmount = allocateFees(repayment, outstandingFees, remainingAmount);

        remainingAmount = allocatePrincipal(repayment, outstandingInstallments, remainingAmount);

        if (remainingAmount.compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalStateException("Unable to allocate full repayment amount");
        }

        updateLoanStatus(loan, outstandingFees, outstandingInstallments);

        loanRepository.save(loan);

        return repayment;
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Repayment amount must be greater than zero");
        }
    }

    private void validateLoan(Loan loan) {
        if (loan.getStatus() != LoanStatus.OPEN && loan.getStatus() != LoanStatus.OVERDUE) {
            throw new IllegalStateException("Loan cannot receive repayment in status: " + loan.getStatus());
        }
    }

    private BigDecimal calculateTotalOutstanding(List<LoanFee> fees, List<Installment> installments) {
        BigDecimal feeOutstanding = fees.stream().map(LoanFee::getOutstandingAmount).reduce(BigDecimal.ZERO,
                BigDecimal::add);

        BigDecimal principalOutstanding = installments.stream().map(Installment::getOutstandingPrincipal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return feeOutstanding.add(principalOutstanding);
    }

    private BigDecimal allocateFees(Repayment repayment, List<LoanFee> fees, BigDecimal remainingAmount) {
        for (LoanFee loanFee : fees) {
            if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal outstanding = loanFee.getOutstandingAmount();

            BigDecimal allocation = remainingAmount.min(outstanding);

            // Update the fee's paid amount
            loanFee.allocatePayment(allocation);
            loanFeeRepository.save(loanFee);

            // Record where this repayment went
            RepaymentAllocation repaymentAllocation = new RepaymentAllocation(repayment, null, loanFee, allocation);
            repaymentAllocationRepository.save(repaymentAllocation);

            remainingAmount = remainingAmount.subtract(allocation);
        }
        return remainingAmount;
    }

    private BigDecimal allocatePrincipal(Repayment repayment, List<Installment> installments,
            BigDecimal remainingAmount) {
        for (Installment installment : installments) {
            if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal outstanding = installment.getOutstandingPrincipal();

            BigDecimal allocation = remainingAmount.min(outstanding);

            installment.allocatePrincipal(allocation);

            installmentRepository.save(installment);

            RepaymentAllocation repaymentAllocation = new RepaymentAllocation(repayment, installment, null, allocation);

            repaymentAllocationRepository.save(repaymentAllocation);

            remainingAmount = remainingAmount.subtract(allocation);
        }
        return remainingAmount;
    }

    private void updateLoanStatus(Loan loan, List<LoanFee> fees, List<Installment> installments) {
        boolean feesOutstanding = fees.stream()
                .anyMatch(fee -> fee.getOutstandingAmount().compareTo(BigDecimal.ZERO) > 0);

        boolean principalOutstanding = installments.stream()
                .anyMatch(installment -> installment.getOutstandingPrincipal().compareTo(BigDecimal.ZERO) > 0);

        if (!feesOutstanding && !principalOutstanding) {
            loan.close();
        }
    }
}
