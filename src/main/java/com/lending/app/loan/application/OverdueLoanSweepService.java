package com.lending.app.loan.application;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lending.app.loan.domain.Loan;
import com.lending.app.loan.domain.LoanFee;
import com.lending.app.loan.domain.LoanTermFee;
import com.lending.app.loan.domain.LoanTerms;
import com.lending.app.loan.repository.LoanFeeRepository;
import com.lending.app.loan.repository.LoanRepository;
import com.lending.app.notification.event.LoanOverdueEvent;
import com.lending.app.product.domain.FeeType;
import com.lending.app.repayment_schedule.domain.Installment;
import com.lending.app.repayment_schedule.domain.InstallmentStatus;
import com.lending.app.repayment_schedule.domain.RepaymentSchedule;
import com.lending.app.repayment_schedule.repository.InstallmentRepository;

@Service
@Transactional
public class OverdueLoanSweepService {

    private final LoanRepository loanRepository;
    private final InstallmentRepository installmentRepository;
    private final LoanFeeRepository loanFeeRepository;
    private final LoanFeeCalculator loanFeeCalculator;
    private final ApplicationEventPublisher eventPublisher;

    public OverdueLoanSweepService(
            LoanRepository loanRepository,
            InstallmentRepository installmentRepository,
            LoanFeeRepository loanFeeRepository,
            LoanFeeCalculator loanFeeCalculator,
            ApplicationEventPublisher eventPublisher) {

        this.loanRepository = loanRepository;
        this.installmentRepository = installmentRepository;
        this.loanFeeRepository = loanFeeRepository;
        this.loanFeeCalculator = loanFeeCalculator;
        this.eventPublisher = eventPublisher;
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

            if (!isOverdue(installment, terms.getGracePeriodDays(), today)) {
                continue;
            }

            if (installment.getStatus() != InstallmentStatus.OVERDUE) {
                installment.markOverdue();
                installmentRepository.save(installment);
            }

            overdue = true;

            applyLateFees(loan, installment, terms, today);
        }

        if (overdue) {
            loan.markOverdue();
            loanRepository.save(loan);

            eventPublisher.publishEvent(new LoanOverdueEvent(loan.getId(), loan.getCustomer().getId()));
        }
    }

    private boolean isOverdue(Installment installment, int gracePeriodDays, LocalDate today) {

        LocalDate overdueDate = installment.getDueDate().plusDays(gracePeriodDays);

        return today.isAfter(overdueDate);
    }

    private void applyLateFees(Loan loan, Installment installment, LoanTerms terms, LocalDate today) {

        long daysAfterDue = ChronoUnit.DAYS.between(installment.getDueDate(), today);

        for (LoanTermFee fee : terms.getFees()) {

            if (fee.getFeeType() != FeeType.LATE) {
                continue;
            }

            if (fee.getTriggerDays() == null) {
                continue;
            }

            if (daysAfterDue < fee.getTriggerDays()) {
                continue;
            }

            applyLateFee(loan, installment, fee, today);
        }
    }

    private void applyLateFee(Loan loan, Installment installment, LoanTermFee fee, LocalDate today) {

        // Used to prevent duplicate late fees for the same installment and fee trigger
        // days
        String reference = "LATE:"
                + loan.getId()
                + ":"
                + installment.getId()
                + ":"
                + fee.getTriggerDays();

        if (loanFeeRepository.existsByReference(reference)) {
            return;
        }

        BigDecimal outstandingPrincipal = installment.getOutstandingPrincipal();

        BigDecimal amount = loanFeeCalculator.calculate(fee, outstandingPrincipal);

        LoanFee loanFee = new LoanFee(
                loan,
                FeeType.LATE,
                amount,
                today,
                installment.getDueDate(),
                reference,
                "Late fee for overdue installment " + installment.getInstallmentNumber());

        loanFeeRepository.save(loanFee);
    }
}
