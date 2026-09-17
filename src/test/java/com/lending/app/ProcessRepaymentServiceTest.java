package com.lending.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
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
import com.lending.app.loan.domain.LoanStatus;
import com.lending.app.loan.repository.LoanFeeRepository;
import com.lending.app.loan.repository.LoanRepository;
import com.lending.app.product.domain.ProcessRepaymentService;
import com.lending.app.repayment.domain.Repayment;
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
}
