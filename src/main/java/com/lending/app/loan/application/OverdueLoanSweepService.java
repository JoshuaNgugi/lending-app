package com.lending.app.loan.application;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lending.app.loan.domain.Loan;
import com.lending.app.loan.domain.LoanFee;
import com.lending.app.loan.domain.LoanStatus;
import com.lending.app.loan.domain.LoanTermFee;
import com.lending.app.loan.domain.LoanTerms;
import com.lending.app.loan.persistence.LoanFeeRepository;
import com.lending.app.loan.persistence.LoanRepository;
import com.lending.app.notification.event.NotificationEvent;
import com.lending.app.notification.event.NotificationEventType;
import com.lending.app.product.domain.FeeType;
import com.lending.app.repayment_schedule.domain.Installment;
import com.lending.app.repayment_schedule.domain.InstallmentStatus;
import com.lending.app.repayment_schedule.domain.RepaymentSchedule;
import com.lending.app.repayment_schedule.persistence.InstallmentRepository;

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

        List<Loan> loans = loanRepository.findActiveLoansWithOutstandingInstallments();

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
            boolean becameOverdue = loan.markOverdue();
            // Check to guarantee state transition
            if (becameOverdue) {

                loanRepository.save(loan);

                eventPublisher.publishEvent(new NotificationEvent(NotificationEventType.LOAN_OVERDUE,
                        loan.getId(), loan.getCustomer().getId(),
                        Map.of()));
            }

            writeOffIfEligible(loan, installments, terms, today);
        }
    }

    private void writeOffIfEligible(
            Loan loan,
            List<Installment> outstandingInstallments,
            LoanTerms terms,
            LocalDate today) {

        Integer writeOffAfterDays = terms.getWriteOffAfterDays();

        if (writeOffAfterDays == null || loan.getStatus() != LoanStatus.OVERDUE) {
            return;
        }

        Optional<LocalDate> oldestOverdueDate = outstandingInstallments.stream()
                .filter(installment -> isOverdue(installment, terms.getGracePeriodDays(), today))
                .map(installment -> installment.getDueDate().plusDays(terms.getGracePeriodDays()))
                .min(LocalDate::compareTo);

        if (oldestOverdueDate.isEmpty()
                || today.isBefore(oldestOverdueDate.get().plusDays(writeOffAfterDays))) {
            return;
        }

        BigDecimal outstandingPrincipal = outstandingInstallments.stream()
                .map(Installment::getOutstandingPrincipal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal outstandingFees = loanFeeRepository.findOutstandingFees(loan.getId()).stream()
                .map(LoanFee::getOutstandingAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (outstandingPrincipal.signum() <= 0 && outstandingFees.signum() <= 0) {
            return;
        }

        loan.writeOff();
        loanRepository.save(loan);
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
