package com.lending.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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

import com.lending.app.loan.application.OverdueLoanSweepService;
import com.lending.app.loan.domain.Loan;
import com.lending.app.loan.domain.LoanTerms;
import com.lending.app.loan.repository.LoanFeeRepository;
import com.lending.app.loan.repository.LoanRepository;
import com.lending.app.loan.repository.LoanTermFeeRepository;
import com.lending.app.product.domain.BillingMode;
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
    private LoanTermFeeRepository loanTermFeeRepository;

    @Mock
    private LoanFeeRepository loanFeeRepository;

    @InjectMocks
    private OverdueLoanSweepService overdueLoanSweepService;

    private UUID loanId;
    private UUID scheduleId;
    private UUID termsId;

    private Loan loan;
    private RepaymentSchedule schedule;
    private LoanTerms terms;

    @BeforeEach
    void setUp() {
        loanId = UUID.randomUUID();
        scheduleId = UUID.randomUUID();
        termsId = UUID.randomUUID();

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
        when(terms.getId()).thenReturn(termsId);

        when(terms.getGracePeriodDays()).thenReturn(3);

        when(loanRepository.findOpenLoansWithOutstandingInstallments()).thenReturn(List.of(loan));

        when(installmentRepository.findOutstandingInstallments(schedule.getId())).thenReturn(List.of(installment));

        when(loanTermFeeRepository.findByLoanTermsId(terms.getId())).thenReturn(List.of());

        overdueLoanSweepService.execute(LocalDate.of(2026, 9, 14));

        assertEquals(InstallmentStatus.OVERDUE, installment.getStatus());

        verify(loan).markOverdue();

        verify(installmentRepository).save(installment);

        verify(loanRepository).save(loan);
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
        when(terms.getId()).thenReturn(termsId);

        when(terms.getGracePeriodDays()).thenReturn(3);

        when(loanRepository.findOpenLoansWithOutstandingInstallments()).thenReturn(List.of(loan));

        when(installmentRepository.findOutstandingInstallments(
                schedule.getId()))
                .thenReturn(List.of(installment));

        when(loanTermFeeRepository.findByLoanTermsId(terms.getId())).thenReturn(List.of());

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
}
