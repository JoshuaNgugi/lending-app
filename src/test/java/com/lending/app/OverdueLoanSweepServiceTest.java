package com.lending.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.lending.app.customer.domain.Customer;
import com.lending.app.customer.domain.CustomerSegment;
import com.lending.app.loan.application.LoanFeeCalculator;
import com.lending.app.loan.application.OverdueLoanSweepService;
import com.lending.app.loan.domain.Loan;
import com.lending.app.loan.domain.LoanFee;
import com.lending.app.loan.domain.LoanStatus;
import com.lending.app.loan.domain.LoanTermFee;
import com.lending.app.loan.domain.LoanTerms;
import com.lending.app.loan.persistence.LoanFeeRepository;
import com.lending.app.loan.persistence.LoanRepository;
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
import com.lending.app.repayment_schedule.persistence.InstallmentRepository;

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
        private Map<String, Object> variables;

        private Loan loan;
        private RepaymentSchedule schedule;
        private LoanTerms terms;

        @BeforeEach
        void setUp() {
                loanId = UUID.randomUUID();
                customerId = UUID.randomUUID();
                scheduleId = UUID.randomUUID();
                variables = new HashMap<>();

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
                when(loan.markOverdue()).thenReturn(true);
        }

        private Loan createOpenLoan() {
                Customer customer = new Customer(
                                "Test",
                                "Customer",
                                "test@example.com",
                                "254700000000",
                                CustomerSegment.RETAIL);
                Loan loan = new Loan(customer, null, new BigDecimal("10000.00"));
                loan.disburse(
                                LocalDate.of(2026, 9, 1),
                                LocalDate.of(2026, 12, 1));
                return loan;
        }

        private LoanTerms createLoanTerms(Loan loan, int gracePeriodDays) {
                return new LoanTerms(
                                loan,
                                3,
                                TenureUnit.MONTHS,
                                LoanStructure.INSTALLMENT,
                                BillingMode.INDIVIDUAL,
                                null,
                                gracePeriodDays,
                                3);
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

                when(loanRepository.findActiveLoansWithOutstandingInstallments()).thenReturn(List.of(loan));

                when(installmentRepository.findOutstandingInstallments(schedule.getId()))
                                .thenReturn(List.of(installment));

                stubLoanIdentity(loan);
                overdueLoanSweepService.execute(LocalDate.of(2026, 9, 14));

                assertEquals(InstallmentStatus.OVERDUE, installment.getStatus());

                verify(loan).markOverdue();

                verify(installmentRepository).save(installment);

                verify(loanRepository).save(loan);

                // Verify that the LoanOverdueEvent is published with the correct loanId and
                // customerId
                verify(eventPublisher)
                                .publishEvent(new NotificationEvent(NotificationEventType.LOAN_OVERDUE, loanId,
                                                customerId, variables));
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

                when(loanRepository.findActiveLoansWithOutstandingInstallments()).thenReturn(List.of(loan));

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
                when(loanRepository.findActiveLoansWithOutstandingInstallments()).thenReturn(List.of(loan));
                installment.allocatePrincipal(new BigDecimal("1000.00"));

                when(loan.getRepaymentSchedule()).thenReturn(schedule);

                when(loan.getLoanTerms()).thenReturn(terms);

                when(schedule.getId()).thenReturn(scheduleId);
                stubLoanIdentity(loan);

                when(terms.getGracePeriodDays()).thenReturn(3);

                when(loanRepository.findActiveLoansWithOutstandingInstallments()).thenReturn(List.of(loan));

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
                when(loanRepository.findActiveLoansWithOutstandingInstallments()).thenReturn(List.of(loan));
                installment.allocatePrincipal(new BigDecimal("3333.33"));

                when(loan.getRepaymentSchedule()).thenReturn(schedule);

                when(loan.getLoanTerms()).thenReturn(terms);

                when(schedule.getId()).thenReturn(scheduleId);

                when(loanRepository.findActiveLoansWithOutstandingInstallments()).thenReturn(List.of(loan));

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

                when(loanRepository.findActiveLoansWithOutstandingInstallments()).thenReturn(List.of(loan));

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

                when(loanRepository.findActiveLoansWithOutstandingInstallments())
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

                when(loanRepository.findActiveLoansWithOutstandingInstallments()).thenReturn(List.of(loan));

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

                when(loanRepository.findActiveLoansWithOutstandingInstallments()).thenReturn(List.of(loan));

                when(installmentRepository.findOutstandingInstallments(scheduleId)).thenReturn(List.of(installment));

                when(loanFeeRepository.existsByReference(anyString())).thenReturn(true);

                overdueLoanSweepService.execute(LocalDate.of(2026, 9, 15));

                verify(loanFeeRepository, never()).save(any(LoanFee.class));

                verifyNoInteractions(loanFeeCalculator);
        }

        @Test
        void shouldApplyLaterLateFeeTriggerAfterLoanBecomesOverdue() {

                Loan loan = createOpenLoan();
                RepaymentSchedule schedule = new RepaymentSchedule(loan);
                loan.setRepaymentSchedule(schedule);

                Installment installment = new Installment(
                                schedule,
                                1,
                                LocalDate.of(2026, 9, 10),
                                new BigDecimal("3333.33"));

                LoanTerms terms = createLoanTerms(loan, 1);
                LoanTermFee lateFee = new LoanTermFee(
                                terms,
                                FeeType.LATE,
                                FeeCalculationType.FIXED,
                                new BigDecimal("200.00"),
                                FeeApplicationTiming.AFTER_DUE_DATE,
                                5);
                terms.addFee(lateFee);
                loan.setLoanTerms(terms);

                when(loanRepository.findActiveLoansWithOutstandingInstallments())
                                .thenReturn(List.of(loan));
                when(installmentRepository.findOutstandingInstallments(schedule.getId()))
                                .thenReturn(List.of(installment));
                when(loanFeeRepository.existsByReference(anyString())).thenReturn(false);
                when(loanFeeCalculator.calculate(lateFee, new BigDecimal("3333.33")))
                                .thenReturn(new BigDecimal("200.00"));

                overdueLoanSweepService.execute(LocalDate.of(2026, 9, 14));
                overdueLoanSweepService.execute(LocalDate.of(2026, 9, 15));

                verify(loanFeeRepository).save(any(LoanFee.class));
                verify(loanFeeCalculator).calculate(lateFee, new BigDecimal("3333.33"));
        }

        @Test
        void shouldPublishLoanOverdueEventWhenLoanBecomesOverdue() {

                LocalDate today = LocalDate.of(2026, 9, 20);

                Loan loan = createOpenLoan();

                RepaymentSchedule schedule = new RepaymentSchedule(loan);

                loan.setRepaymentSchedule(schedule);

                Installment installment = new Installment(
                                schedule,
                                1,
                                LocalDate.of(2026, 9, 18),
                                new BigDecimal("1000.00"));

                when(loanRepository.findActiveLoansWithOutstandingInstallments())
                                .thenReturn(List.of(loan));

                when(installmentRepository.findOutstandingInstallments(schedule.getId()))
                                .thenReturn(List.of(installment));

                LoanTerms terms = createLoanTerms(
                                loan,
                                1 // grace period
                );

                loan.setLoanTerms(terms);

                overdueLoanSweepService.execute(today);

                verify(eventPublisher).publishEvent(
                                argThat((Object event) -> event instanceof NotificationEvent notificationEvent
                                                && notificationEvent.eventType() == NotificationEventType.LOAN_OVERDUE
                                                && java.util.Objects.equals(notificationEvent.loanId(), loan.getId())
                                                && java.util.Objects.equals(notificationEvent.customerId(),
                                                                loan.getCustomer().getId())));
        }

        @Test
        void shouldNotPublishOverdueEventWhenLoanIsAlreadyOverdue() {

                LocalDate today = LocalDate.of(2026, 9, 20);

                Loan loan = createOpenLoan();

                RepaymentSchedule schedule = new RepaymentSchedule(loan);

                loan.setRepaymentSchedule(schedule);

                // Make the loan already overdue.
                loan.markOverdue();

                Installment installment = new Installment(
                                schedule,
                                1,
                                LocalDate.of(2026, 9, 18),
                                new BigDecimal("1000.00"));

                when(loanRepository.findActiveLoansWithOutstandingInstallments())
                                .thenReturn(List.of(loan));

                when(installmentRepository.findOutstandingInstallments(schedule.getId()))
                                .thenReturn(List.of(installment));

                LoanTerms terms = createLoanTerms(loan, 1);
                loan.setLoanTerms(terms);

                overdueLoanSweepService.execute(today);

                verify(eventPublisher, never()).publishEvent(any(NotificationEvent.class));
        }

        @Test
        void shouldWriteOffOverdueLoanAfterConfiguredPolicyAge() {

                Loan loan = createOpenLoan();
                RepaymentSchedule schedule = new RepaymentSchedule(loan);
                loan.setRepaymentSchedule(schedule);

                Installment installment = new Installment(
                                schedule,
                                1,
                                LocalDate.of(2026, 9, 10),
                                new BigDecimal("1000.00"));
                LoanTerms terms = new LoanTerms(
                                loan,
                                3,
                                TenureUnit.MONTHS,
                                LoanStructure.INSTALLMENT,
                                BillingMode.INDIVIDUAL,
                                null,
                                3,
                                3,
                                90);
                loan.setLoanTerms(terms);

                when(loanRepository.findActiveLoansWithOutstandingInstallments()).thenReturn(List.of(loan));
                when(installmentRepository.findOutstandingInstallments(schedule.getId()))
                                .thenReturn(List.of(installment));
                when(loanFeeRepository.findOutstandingFees(loan.getId())).thenReturn(List.of());

                overdueLoanSweepService.execute(LocalDate.of(2026, 12, 13));

                assertEquals(LoanStatus.WRITTEN_OFF, loan.getStatus());
                verify(eventPublisher).publishEvent(any(NotificationEvent.class));
                verify(loanRepository, org.mockito.Mockito.times(2)).save(loan);
        }

        @Test
        void shouldNotWriteOffBeforeConfiguredPolicyAge() {

                Loan loan = createOpenLoan();
                RepaymentSchedule schedule = new RepaymentSchedule(loan);
                loan.setRepaymentSchedule(schedule);

                Installment installment = new Installment(
                                schedule,
                                1,
                                LocalDate.of(2026, 9, 10),
                                new BigDecimal("1000.00"));
                loan.setLoanTerms(new LoanTerms(
                                loan,
                                3,
                                TenureUnit.MONTHS,
                                LoanStructure.INSTALLMENT,
                                BillingMode.INDIVIDUAL,
                                null,
                                3,
                                3,
                                90));

                when(loanRepository.findActiveLoansWithOutstandingInstallments()).thenReturn(List.of(loan));
                when(installmentRepository.findOutstandingInstallments(schedule.getId()))
                                .thenReturn(List.of(installment));

                overdueLoanSweepService.execute(LocalDate.of(2026, 12, 11));

                assertEquals(LoanStatus.OVERDUE, loan.getStatus());
                verify(loanRepository).save(loan);
                verify(loanFeeRepository, never()).findOutstandingFees(loan.getId());
        }

        @Test
        void shouldNotWriteOffWhenPolicyIsDisabled() {

                Loan loan = createOpenLoan();
                RepaymentSchedule schedule = new RepaymentSchedule(loan);
                loan.setRepaymentSchedule(schedule);

                Installment installment = new Installment(
                                schedule,
                                1,
                                LocalDate.of(2026, 9, 10),
                                new BigDecimal("1000.00"));
                loan.setLoanTerms(createLoanTerms(loan, 3));

                when(loanRepository.findActiveLoansWithOutstandingInstallments()).thenReturn(List.of(loan));
                when(installmentRepository.findOutstandingInstallments(schedule.getId()))
                                .thenReturn(List.of(installment));

                overdueLoanSweepService.execute(LocalDate.of(2027, 1, 1));

                assertEquals(LoanStatus.OVERDUE, loan.getStatus());
                verify(loanFeeRepository, never()).findOutstandingFees(loan.getId());
        }
}
