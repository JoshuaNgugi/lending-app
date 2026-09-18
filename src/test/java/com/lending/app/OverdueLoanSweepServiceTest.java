package com.lending.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.lending.app.customer.domain.Customer;
import com.lending.app.loan.application.LoanFeeCalculator;
import com.lending.app.loan.application.OverdueLoanSweepService;
import com.lending.app.loan.domain.Loan;
import com.lending.app.loan.domain.LoanFee;
import com.lending.app.loan.domain.LoanTermFee;
import com.lending.app.loan.domain.LoanTerms;
import com.lending.app.loan.repository.LoanFeeRepository;
import com.lending.app.loan.repository.LoanRepository;
import com.lending.app.notification.event.NotificationEvent;
import com.lending.app.notification.event.NotificationEventType;
import com.lending.app.product.domain.BillingMode;
import com.lending.app.product.domain.FeeApplicationTiming;
import com.lending.app.product.domain.FeeCalculationType;
import com.lending.app.product.domain.FeeType;
import com.lending.app.product.domain.LoanStructure;
import com.lending.app.product.domain.TenureUnit;
import com.lending.app.repayment_schedule.domain.Installment;
import com.lending.app.repayment_schedule.domain.InstallmentStatus;
import com.lending.app.repayment_schedule.domain.RepaymentSchedule;
import com.lending.app.repayment_schedule.repository.InstallmentRepository;

@ExtendWith(MockitoExtension.class)
class OverdueLoanSweepServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private InstallmentRepository installmentRepository;

    @Mock
    private LoanFeeRepository loanFeeRepository;

    @Mock
    private LoanFeeCalculator loanFeeCalculator;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private OverdueLoanSweepService overdueLoanSweepService;

    private UUID loanId;
    private UUID customerId;
    private UUID scheduleId;

    private Loan loan;
    private RepaymentSchedule schedule;
    private LoanTerms terms;

    @BeforeEach
    void setUp() {
        loanId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        scheduleId = UUID.randomUUID();

        loan = new Loan(null, null, new BigDecimal("10000.00"));

        loan.disburse(
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 12, 1));

        schedule = new RepaymentSchedule(loan);
        terms = new LoanTerms(
                loan,
                3,
                TenureUnit.MONTHS,
                LoanStructure.INSTALLMENT,
                BillingMode.INDIVIDUAL,
                null,
                3,
                3);

        loan.setRepaymentSchedule(schedule);
        loan.setLoanTerms(terms);
    }

    private void stubLoanIdentity(Loan loan) {
        Customer customer = mock(Customer.class);

        when(loan.getId()).thenReturn(loanId);
        when(loan.getCustomer()).thenReturn(customer);
        when(customer.getId()).thenReturn(customerId);
    }

    /**
     * Test to verify that a loan is marked as overdue when an installment is past
     * the grace period.
     * 
     * Example: If the installment due date is September 10, 2026, and the grace
     * period is 3 days,
     * the installment becomes overdue on September 13, 2026.
     * 
     * This test ensures that the loan is marked as overdue and the installment
     * status is updated accordingly.
     */
    @Test
    void shouldMarkLoanOverdueWhenInstallmentIsPastGracePeriod() {

        Loan loan = mock(Loan.class);
        RepaymentSchedule schedule = mock(RepaymentSchedule.class);
        LoanTerms terms = mock(LoanTerms.class);

        Installment installment = new Installment(
                schedule,
                1,
                LocalDate.of(2026, 9, 10),
                new BigDecimal("3333.33"));

        when(loan.getRepaymentSchedule()).thenReturn(schedule);

        when(loan.getLoanTerms()).thenReturn(terms);

        when(schedule.getId()).thenReturn(scheduleId);

        when(terms.getGracePeriodDays()).thenReturn(3);

        when(loanRepository.findOpenLoansWithOutstandingInstallments()).thenReturn(List.of(loan));

        when(installmentRepository.findOutstandingInstallments(schedule.getId())).thenReturn(List.of(installment));

        stubLoanIdentity(loan);
        overdueLoanSweepService.execute(LocalDate.of(2026, 9, 14));

        assertEquals(InstallmentStatus.OVERDUE, installment.getStatus());

        verify(loan).markOverdue();

        verify(installmentRepository).save(installment);

        verify(loanRepository).save(loan);

        // Verify that the LoanOverdueEvent is published with the correct loanId and
        // customerId
        verify(eventPublisher)
                .publishEvent(new NotificationEvent(NotificationEventType.LOAN_OVERDUE, loanId, customerId));
    }

    @Test
    void shouldNotMarkLoanOverdueDuringGracePeriod() {

        Loan loan = mock(Loan.class);
        RepaymentSchedule schedule = mock(RepaymentSchedule.class);
        LoanTerms terms = mock(LoanTerms.class);

        Installment installment = new Installment(
                schedule,
                1,
                LocalDate.of(2026, 9, 10),
                new BigDecimal("3333.33"));

        when(loan.getRepaymentSchedule()).thenReturn(schedule);

        when(loan.getLoanTerms()).thenReturn(terms);

        when(schedule.getId()).thenReturn(scheduleId);

        when(terms.getGracePeriodDays()).thenReturn(3);

        when(loanRepository.findOpenLoansWithOutstandingInstallments()).thenReturn(List.of(loan));

        when(installmentRepository.findOutstandingInstallments(
                schedule.getId()))
                .thenReturn(List.of(installment));

        overdueLoanSweepService.execute(LocalDate.of(2026, 9, 13));

        assertEquals(InstallmentStatus.PENDING, installment.getStatus());

        verify(loan, never()).markOverdue();

        verify(loanRepository, never()).save(loan);

        verify(installmentRepository, never()).save(installment);
    }

    @Test
    void shouldMarkPartiallyPaidInstallmentOverdue() {

        Loan loan = mock(Loan.class);
        RepaymentSchedule schedule = mock(RepaymentSchedule.class);
        LoanTerms terms = mock(LoanTerms.class);

        Installment installment = new Installment(
                schedule,
                1,
                LocalDate.of(2026, 9, 10),
                new BigDecimal("3333.33"));

        installment.allocatePrincipal(new BigDecimal("1000.00"));

        when(loan.getRepaymentSchedule()).thenReturn(schedule);

        when(loan.getLoanTerms()).thenReturn(terms);

        when(schedule.getId()).thenReturn(scheduleId);
        stubLoanIdentity(loan);

        when(terms.getGracePeriodDays()).thenReturn(3);

        when(loanRepository.findOpenLoansWithOutstandingInstallments()).thenReturn(List.of(loan));

        when(installmentRepository.findOutstandingInstallments(
                schedule.getId()))
                .thenReturn(List.of(installment));

        overdueLoanSweepService.execute(LocalDate.of(2026, 9, 14));

        assertEquals(InstallmentStatus.OVERDUE, installment.getStatus());

        assertEquals(new BigDecimal("2333.33"), installment.getOutstandingPrincipal());

        verify(loan).markOverdue();
    }

    @Test
    void shouldIgnorePaidInstallments() {

        Loan loan = mock(Loan.class);
        RepaymentSchedule schedule = mock(RepaymentSchedule.class);
        LoanTerms terms = mock(LoanTerms.class);

        Installment installment = new Installment(
                schedule,
                1,
                LocalDate.of(2026, 9, 10),
                new BigDecimal("3333.33"));

        installment.allocatePrincipal(new BigDecimal("3333.33"));

        when(loan.getRepaymentSchedule()).thenReturn(schedule);

        when(loan.getLoanTerms()).thenReturn(terms);

        when(schedule.getId()).thenReturn(scheduleId);

        when(loanRepository.findOpenLoansWithOutstandingInstallments()).thenReturn(List.of(loan));

        when(installmentRepository.findOutstandingInstallments(schedule.getId())).thenReturn(List.of());

        overdueLoanSweepService.execute(LocalDate.of(2026, 9, 14));

        verify(loan, never()).markOverdue();
        verify(loanRepository, never()).save(loan);
    }

    /**
     * Test to verify that a late fee is applied when the trigger days for the late
     * fee are reached.
     * 
     * Example: If the installment due date is September 10, 2026, and the grace
     * period is 3 days,
     * the installment becomes overdue on September 13, 2026.
     * If a late fee has a trigger of 5 days after the due date, it should be
     * applied on September 15, 2026.
     */
    @Test
    void shouldApplyLateFeeWhenTriggerDaysAreReached() {

        Loan loan = mock(Loan.class);
        RepaymentSchedule schedule = mock(RepaymentSchedule.class);
        LoanTerms terms = mock(LoanTerms.class);

        Installment installment = new Installment(
                schedule,
                1,
                LocalDate.of(2026, 9, 10),
                new BigDecimal("3333.33"));

        LoanTermFee lateFee = new LoanTermFee(
                terms,
                FeeType.LATE,
                FeeCalculationType.FIXED,
                new BigDecimal("200.00"),
                FeeApplicationTiming.AFTER_DUE_DATE,
                5);

        when(loan.getRepaymentSchedule()).thenReturn(schedule);
        when(loan.getLoanTerms()).thenReturn(terms);

        when(schedule.getId()).thenReturn(scheduleId);
        stubLoanIdentity(loan);

        when(terms.getGracePeriodDays()).thenReturn(3);
        when(terms.getFees()).thenReturn(List.of(lateFee));

        when(loanRepository.findOpenLoansWithOutstandingInstallments()).thenReturn(List.of(loan));

        when(installmentRepository.findOutstandingInstallments(scheduleId)).thenReturn(List.of(installment));

        when(loanFeeRepository.existsByReference(anyString())).thenReturn(false);

        when(loanFeeCalculator.calculate(
                eq(lateFee),
                eq(new BigDecimal("3333.33"))))
                .thenReturn(new BigDecimal("200.00"));

        overdueLoanSweepService.execute(LocalDate.of(2026, 9, 15));

        verify(loanFeeRepository).save(any(LoanFee.class));

        verify(loanFeeCalculator).calculate(eq(lateFee), eq(new BigDecimal("3333.33")));
    }

    /**
     * Test to verify that a late fee is not applied before the trigger days for the
     * late fee are reached.
     * 
     * Example: If the installment due date is September 10, 2026, and the grace
     * period is 3 days,
     * the installment becomes overdue on September 13, 2026.
     * If a late fee has a trigger of 5 days after the due date, it should not be
     * applied on September 14, 2026.
     */
    @Test
    void shouldNotApplyLateFeeBeforeTriggerDays() {

        Loan loan = mock(Loan.class);
        RepaymentSchedule schedule = mock(RepaymentSchedule.class);
        LoanTerms terms = mock(LoanTerms.class);

        Installment installment = new Installment(
                schedule,
                1,
                LocalDate.of(2026, 9, 10),
                new BigDecimal("3333.33"));

        LoanTermFee lateFee = new LoanTermFee(
                terms,
                FeeType.LATE,
                FeeCalculationType.FIXED,
                new BigDecimal("200.00"),
                FeeApplicationTiming.AFTER_DUE_DATE,
                5);

        when(loan.getRepaymentSchedule()).thenReturn(schedule);
        when(loan.getLoanTerms()).thenReturn(terms);

        when(schedule.getId()).thenReturn(scheduleId);
        stubLoanIdentity(loan);

        when(terms.getGracePeriodDays()).thenReturn(3);
        when(terms.getFees()).thenReturn(List.of(lateFee));

        when(loanRepository.findOpenLoansWithOutstandingInstallments())
                .thenReturn(List.of(loan));

        when(installmentRepository.findOutstandingInstallments(scheduleId))
                .thenReturn(List.of(installment));

        overdueLoanSweepService.execute(LocalDate.of(2026, 9, 14));

        verify(loanFeeRepository, never()).save(any(LoanFee.class));

        verifyNoInteractions(loanFeeCalculator);
    }

    /**
     * Test to verify that a late fee is applied exactly on the trigger day for the
     * late fee.
     * 
     * Example: If the installment due date is September 10, 2026, and the grace
     * period is 3 days,
     * the installment becomes overdue on September 13, 2026.
     * If a late fee has a trigger of 5 days after the due date, it should be
     * applied on September 15, 2026.
     * 
     * This test ensures that the late fee is applied on the correct day and not
     * before or after.
     * 
     */
    @Test
    void shouldApplyLateFeeExactlyOnTriggerDay() {

        Loan loan = mock(Loan.class);
        RepaymentSchedule schedule = mock(RepaymentSchedule.class);
        LoanTerms terms = mock(LoanTerms.class);

        Installment installment = new Installment(
                schedule,
                1,
                LocalDate.of(2026, 9, 10),
                new BigDecimal("3333.33"));

        LoanTermFee lateFee = new LoanTermFee(
                terms,
                FeeType.LATE,
                FeeCalculationType.FIXED,
                new BigDecimal("200.00"),
                FeeApplicationTiming.AFTER_DUE_DATE,
                5);

        stubLoanIdentity(loan);
        when(loan.getRepaymentSchedule()).thenReturn(schedule);
        when(loan.getLoanTerms()).thenReturn(terms);

        when(schedule.getId()).thenReturn(scheduleId);

        when(terms.getGracePeriodDays()).thenReturn(3);
        when(terms.getFees()).thenReturn(List.of(lateFee));

        when(loanRepository.findOpenLoansWithOutstandingInstallments()).thenReturn(List.of(loan));

        when(installmentRepository.findOutstandingInstallments(scheduleId)).thenReturn(List.of(installment));

        when(loanFeeRepository.existsByReference(anyString())).thenReturn(false);

        when(loanFeeCalculator.calculate(
                eq(lateFee),
                eq(new BigDecimal("3333.33"))))
                .thenReturn(new BigDecimal("200.00"));

        overdueLoanSweepService.execute(LocalDate.of(2026, 9, 15));

        verify(loanFeeRepository).save(any(LoanFee.class));
    }

    /**
     * Test to verify that a late fee is not applied if it has already been applied
     * for the same installment and fee trigger days.
     * 
     * This test ensures that duplicate late fees are not created for the same
     * installment and fee trigger days.
     * 
     */
    @Test
    void shouldNotCreateDuplicateLateFee() {

        Loan loan = mock(Loan.class);
        RepaymentSchedule schedule = mock(RepaymentSchedule.class);
        LoanTerms terms = mock(LoanTerms.class);

        Installment installment = new Installment(
                schedule,
                1,
                LocalDate.of(2026, 9, 10),
                new BigDecimal("3333.33"));

        LoanTermFee lateFee = new LoanTermFee(
                terms,
                FeeType.LATE,
                FeeCalculationType.FIXED,
                new BigDecimal("200.00"),
                FeeApplicationTiming.AFTER_DUE_DATE,
                5);

        stubLoanIdentity(loan);
        when(loan.getRepaymentSchedule()).thenReturn(schedule);
        when(loan.getLoanTerms()).thenReturn(terms);

        when(schedule.getId()).thenReturn(scheduleId);

        when(terms.getGracePeriodDays()).thenReturn(3);
        when(terms.getFees()).thenReturn(List.of(lateFee));

        when(loanRepository.findOpenLoansWithOutstandingInstallments()).thenReturn(List.of(loan));

        when(installmentRepository.findOutstandingInstallments(scheduleId)).thenReturn(List.of(installment));

        when(loanFeeRepository.existsByReference(anyString())).thenReturn(true);

        overdueLoanSweepService.execute(LocalDate.of(2026, 9, 15));

        verify(loanFeeRepository, never()).save(any(LoanFee.class));

        verifyNoInteractions(loanFeeCalculator);
    }
}
