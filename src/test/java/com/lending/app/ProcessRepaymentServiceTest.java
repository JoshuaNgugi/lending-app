package com.lending.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lending.app.loan.domain.Loan;
import com.lending.app.loan.domain.LoanFee;
import com.lending.app.loan.domain.LoanStatus;
import com.lending.app.loan.repository.LoanFeeRepository;
import com.lending.app.loan.repository.LoanRepository;
import com.lending.app.product.domain.FeeType;
import com.lending.app.product.domain.ProcessRepaymentService;
import com.lending.app.repayment.domain.Repayment;
import com.lending.app.repayment.domain.RepaymentAllocation;
import com.lending.app.repayment.repayment.RepaymentAllocationRepository;
import com.lending.app.repayment.repayment.RepaymentRepository;
import com.lending.app.repayment_schedule.domain.Installment;
import com.lending.app.repayment_schedule.domain.InstallmentStatus;
import com.lending.app.repayment_schedule.domain.RepaymentSchedule;
import com.lending.app.repayment_schedule.repository.InstallmentRepository;

@ExtendWith(MockitoExtension.class)
public class ProcessRepaymentServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private RepaymentRepository repaymentRepository;

    @Mock
    private RepaymentAllocationRepository repaymentAllocationRepository;

    @Mock
    private LoanFeeRepository loanFeeRepository;

    @Mock
    private InstallmentRepository installmentRepository;

    @InjectMocks
    private ProcessRepaymentService processRepaymentService;

    private UUID loanId;
    private Loan loan;
    private RepaymentSchedule repaymentSchedule;

    @BeforeEach
    void setUp() {
        loanId = UUID.randomUUID();
        loan = new Loan(null, null, new BigDecimal("10000.00"));

        loan.disburse(LocalDate.of(2026, 9, 17), LocalDate.of(2026, 12, 17));

        repaymentSchedule = new RepaymentSchedule(loan);

        loan.setRepaymentSchedule(repaymentSchedule);
    }

    /**
     * Test to ensure that a partial repayment is processed correctly.
     */
    @Test
    void shouldProcessPartialRepayment() {

        Installment installment1 = new Installment(repaymentSchedule, 1,
                LocalDate.of(2026, 10, 17), new BigDecimal("3333.33"));

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));

        when(repaymentRepository.existsByReference("PAY-001")).thenReturn(false);

        when(installmentRepository.findOutstandingInstallments(repaymentSchedule.getId()))
                .thenReturn(List.of(installment1));

        when(repaymentRepository.save(any(Repayment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Repayment repayment = processRepaymentService.execute(loanId, new BigDecimal("1000.00"), "PAY-001", "MPESA");

        assertNotNull(repayment);
        assertEquals(new BigDecimal("1000.00"), repayment.getAmount());
        assertEquals("PAY-001", repayment.getReference());
        assertEquals("MPESA", repayment.getChannel());

        assertEquals(new BigDecimal("1000.00"), installment1.getPrincipalPaid());

        assertEquals(new BigDecimal("2333.33"), installment1.getOutstandingPrincipal());

        assertEquals(InstallmentStatus.PARTIALLY_PAID, installment1.getStatus());

        assertEquals(InstallmentStatus.PARTIALLY_PAID, installment1.getStatus());

        assertEquals(LoanStatus.OPEN, loan.getStatus());
    }

    /**
     * Test to ensure that fees are allocated before principal when processing a
     * repayment.
     */
    @Test
    void shouldAllocateFeesBeforePrincipal() {
        LoanFee fee = new LoanFee(loan, FeeType.SERVICE, new BigDecimal("500.00"),
                LocalDate.of(2026, 9, 17), null,
                "Original service fee", null);

        Installment installment1 = new Installment(repaymentSchedule, 1,
                LocalDate.of(2026, 10, 17), new BigDecimal("3333.33"));

        Installment installment2 = new Installment(repaymentSchedule, 2,
                LocalDate.of(2026, 11, 17), new BigDecimal("3333.33"));

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));

        when(repaymentRepository.existsByReference("PAY-002")).thenReturn(false);

        when(loanFeeRepository.findOutstandingFees(loanId)).thenReturn(List.of(fee));

        when(installmentRepository.findOutstandingInstallments(repaymentSchedule.getId()))
                .thenReturn(List.of(installment1, installment2));

        when(repaymentRepository.save(any(Repayment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        processRepaymentService.execute(loanId, new BigDecimal("4000.00"), "PAY-002", "MPESA");

        assertEquals(new BigDecimal("500.00"), fee.getAmountPaid());

        assertEquals(new BigDecimal("3333.33"), installment1.getPrincipalPaid());

        assertEquals(new BigDecimal("166.67"), installment2.getPrincipalPaid());

        assertEquals(InstallmentStatus.PAID, installment1.getStatus());
        assertEquals(InstallmentStatus.PARTIALLY_PAID, installment2.getStatus());

        assertEquals(LoanStatus.OPEN, loan.getStatus());

        // Verify that the repayment allocations were created correctly
        var allocationCaptor = org.mockito.ArgumentCaptor.forClass(RepaymentAllocation.class);

        verify(repaymentAllocationRepository, times(3))
                .save(allocationCaptor.capture());

        List<RepaymentAllocation> allocations = allocationCaptor.getAllValues();

        assertEquals(3, allocations.size());

        assertEquals(new BigDecimal("500.00"), allocations.get(0).getAmount());

        assertSame(fee, allocations.get(0).getLoanFee());

        assertNull(allocations.get(0).getInstallment());

        assertEquals(new BigDecimal("3333.33"), allocations.get(1).getAmount());

        assertSame(installment1, allocations.get(1).getInstallment());

        assertEquals(new BigDecimal("166.67"), allocations.get(2).getAmount());

        assertSame(installment2, allocations.get(2).getInstallment());
    }
}
