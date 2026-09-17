package com.lending.app.loan.application;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lending.app.loan.domain.Loan;
import com.lending.app.loan.domain.LoanFee;
import com.lending.app.loan.domain.LoanTermFee;
import com.lending.app.loan.domain.LoanTerms;
import com.lending.app.loan.repository.LoanFeeRepository;
import com.lending.app.loan.repository.LoanRepository;
import com.lending.app.loan.repository.LoanTermFeeRepository;
import com.lending.app.product.domain.FeeType;
import com.lending.app.repayment_schedule.domain.Installment;
import com.lending.app.repayment_schedule.domain.RepaymentSchedule;
import com.lending.app.repayment_schedule.repository.InstallmentRepository;

@Service
public class OverdueLoanSweepService {

    private final LoanRepository loanRepository;
    private final InstallmentRepository installmentRepository;
    private final LoanTermFeeRepository loanTermFeeRepository;
    private final LoanFeeRepository loanFeeRepository;
    private final LoanFeeCalculator loanFeeCalculator;

    public OverdueLoanSweepService(
            LoanRepository loanRepository,
            InstallmentRepository installmentRepository,
            LoanTermFeeRepository loanTermFeeRepository,
            LoanFeeRepository loanFeeRepository,
            LoanFeeCalculator loanFeeCalculator) {

        this.loanRepository = loanRepository;
        this.installmentRepository = installmentRepository;
        this.loanTermFeeRepository = loanTermFeeRepository;
        this.loanFeeRepository = loanFeeRepository;
        this.loanFeeCalculator = loanFeeCalculator;
    }

    @Transactional
    public void execute(LocalDate today) {

        List<Loan> loans = loanRepository.findOpenLoansWithOutstandingInstallments();

        for (Loan loan : loans) {
            processLoan(loan, today);
        }
    }

    private void processLoan(Loan loan, LocalDate today) {

        RepaymentSchedule schedule = loan.getRepaymentSchedule();

        if (schedule == null) {
            return;
        }

        LoanTerms terms = loan.getLoanTerms();

        if (terms == null) {
            return;
        }

        List<Installment> installments = installmentRepository.findOutstandingInstallments(schedule.getId());

        boolean overdue = false;

        for (Installment installment : installments) {
            if (isOverdue(
                    installment,
                    terms.getGracePeriodDays(),
                    today)) {

                installment.markOverdue();
                installmentRepository.save(installment);

                overdue = true;
            }
        }

        if (!overdue) {
            return;
        }

        loan.markOverdue();

        loanRepository.save(loan);

        applyLateFees(loan, terms, installments, today);
    }

    private boolean isOverdue(Installment installment, int gracePeriodDays, LocalDate today) {

        LocalDate overdueDate = installment.getDueDate().plusDays(gracePeriodDays);

        return today.isAfter(overdueDate);
    }

    private void applyLateFees(Loan loan, LoanTerms terms, List<Installment> installments, LocalDate today) {

        List<LoanTermFee> termFees = loanTermFeeRepository.findByLoanTermsId(terms.getId());

        List<LoanTermFee> lateFeeRules = termFees.stream()
                .filter(fee -> fee.getFeeType() == FeeType.LATE)
                .toList();

        for (LoanTermFee lateFeeRule : lateFeeRules) {
            applyLateFee(loan, lateFeeRule, installments, today);
        }
    }

    private void applyLateFee(Loan loan, LoanTermFee lateFeeRule, List<Installment> installments, LocalDate today) {

        for (Installment installment : installments) {

            int daysAfterDue = (int) (today.toEpochDay() - installment.getDueDate().toEpochDay());

            if (daysAfterDue < lateFeeRule.getTriggerDays()) {
                continue;
            }

            // Check if a late fee has already been applied for this installment
            String reference = "LATE:" + loan.getId() + ":" + installment.getId() + ":" + lateFeeRule.getTriggerDays();

            if (loanFeeRepository.existsByReference(reference)) {
                continue;
            }

            BigDecimal amount = loanFeeCalculator.calculate(lateFeeRule, installment.getOutstandingPrincipal());

            LoanFee loanFee = new LoanFee(
                    loan,
                    FeeType.LATE,
                    amount,
                    today,
                    installment.getDueDate(),
                    reference,
                    "Late payment fee for installment " + installment.getInstallmentNumber());

            loanFeeRepository.save(loanFee);
        }
    }
}
