package com.lending.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.lending.app.loan.domain.Loan;
import com.lending.app.loan.domain.LoanFee;
import com.lending.app.loan.domain.LoanStatus;
import com.lending.app.loan.persistence.LoanFeeRepository;
import com.lending.app.loan.persistence.LoanRepository;
import com.lending.app.customer.domain.Customer;
import com.lending.app.customer.domain.CustomerSegment;
import com.lending.app.notification.event.NotificationEvent;
import com.lending.app.notification.event.NotificationEventType;
import com.lending.app.product.domain.FeeType;
import com.lending.app.repayment.application.ProcessRepaymentService;
import com.lending.app.repayment.domain.Repayment;
import com.lending.app.repayment.domain.RepaymentAllocation;
import com.lending.app.repayment.persistence.RepaymentAllocationRepository;
import com.lending.app.repayment.persistence.RepaymentRepository;
import com.lending.app.repayment_schedule.domain.Installment;
import com.lending.app.repayment_schedule.domain.InstallmentStatus;
import com.lending.app.repayment_schedule.domain.RepaymentSchedule;
import com.lending.app.repayment_schedule.persistence.InstallmentRepository;
import com.lending.app.shared.exception.ResourceNotFoundException;

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

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ProcessRepaymentService processRepaymentService;

    private UUID loanId;
    private Loan loan;
    private RepaymentSchedule repaymentSchedule;

    @BeforeEach
    void setUp() {
        loanId = UUID.randomUUID();
        Customer customer = new Customer(
                "Maimuna",
                "Maksuudi",
                "maimuna@email.com",
                "254700000000",
                CustomerSegment.RETAIL);
        loan = new Loan(customer, null, new BigDecimal("10000.00"));

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

        when(loanRepository.findByIdForUpdate(loanId)).thenReturn(Optional.of(loan));

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

        when(loanRepository.findByIdForUpdate(loanId)).thenReturn(Optional.of(loan));

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

    /**
     * Test to ensure that the loan is closed when all outstanding amounts are paid.
     */
    @Test
    void shouldCloseLoanWhenAllOutstandingAmountsArePaid() {
        Installment installment = new Installment(repaymentSchedule, 1,
                LocalDate.of(2026, 10, 17), new BigDecimal("1000.00"));

        when(loanRepository.findByIdForUpdate(loanId)).thenReturn(Optional.of(loan));

        when(repaymentRepository.existsByReference("PAY-003")).thenReturn(false);

        when(loanFeeRepository.findOutstandingFees(loanId)).thenReturn(List.of());

        when(installmentRepository.findOutstandingInstallments(repaymentSchedule.getId()))
                .thenReturn(List.of(installment));

        when(repaymentRepository.save(any(Repayment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        processRepaymentService.execute(loanId, new BigDecimal("1000.00"), "PAY-003", "MPESA");

        assertEquals(InstallmentStatus.PAID, installment.getStatus());
        assertEquals(new BigDecimal("1000.00"), installment.getPrincipalPaid());
        assertEquals(LoanStatus.CLOSED, loan.getStatus());
        verify(loanRepository).save(loan);
    }

    /**
     * Test to ensure that an overpayment is rejected and does not affect the
     * installment or loan status.
     */
    @Test
    void shouldRejectOverPayment() {
        Installment installment = new Installment(repaymentSchedule, 1,
                LocalDate.of(2026, 10, 17), new BigDecimal("1000.00"));

        when(loanRepository.findByIdForUpdate(loanId)).thenReturn(Optional.of(loan));

        when(repaymentRepository.existsByReference("PAY-004")).thenReturn(false);

        when(loanFeeRepository.findOutstandingFees(loanId)).thenReturn(List.of());

        when(installmentRepository.findOutstandingInstallments(repaymentSchedule.getId()))
                .thenReturn(List.of(installment));

        assertThrows(IllegalArgumentException.class, () -> {
            processRepaymentService.execute(loanId, new BigDecimal("1500.00"), "PAY-004", "MPESA");
        });

        // Verify that no repayment was saved due to the overpayment
        verify(repaymentRepository, never()).save(any(Repayment.class));

        assertEquals(BigDecimal.ZERO, installment.getPrincipalPaid());
    }

    /**
     * Test to ensure that a repayment with a duplicate reference is rejected.
     */
    @Test
    void shouldRejectDuplicateRepaymentReference() {
        when(loanRepository.findByIdForUpdate(loanId)).thenReturn(Optional.of(loan));

        when(repaymentRepository.existsByReference("PAY-005")).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> {
            processRepaymentService.execute(loanId, new BigDecimal("500.00"), "PAY-005", "MPESA");
        });

        // Verify that no repayment was saved due to the duplicate reference
        verify(repaymentRepository, never()).save(any(Repayment.class));

        // Verify that no fees were allocated due to the duplicate reference
        verify(loanFeeRepository, never()).findOutstandingFees(any(UUID.class));

        // Verify that no installments were allocated due to the duplicate reference
        verify(installmentRepository, never()).findOutstandingInstallments(any(UUID.class));
    }

    /**
     * Test to ensure that a repayment for a closed loan is rejected.
     */
    @Test
    void shouldRejectRepaymentForClosedLoan() {

        loan.close();

        when(loanRepository.findByIdForUpdate(loanId)).thenReturn(Optional.of(loan));

        assertThrows(IllegalStateException.class, () -> {
            processRepaymentService.execute(loanId, new BigDecimal("500.00"), "PAY-006", "MPESA");
        });

        // Verify that no repayment was saved due to the closed loan
        verify(repaymentRepository, never()).save(any(Repayment.class));
    }

    /**
     * Test to ensure that a repayment for a non-existent loan is rejected.
     */
    @Test
    void shouldRejectRepaymentForNonExistentLoan() {
        when(loanRepository.findByIdForUpdate(loanId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            processRepaymentService.execute(loanId, new BigDecimal("500.00"), "PAY-007", "MPESA");
        });

        // Verify that no repayment was saved due to the non-existent loan
        verify(repaymentRepository, never()).save(any(Repayment.class));
    }

    /**
     * Test to ensure that a repayment with zero amount is rejected.
     */
    @Test
    void shouldRejectZeroRepayment() {
        assertThrows(IllegalArgumentException.class, () -> {
            processRepaymentService.execute(loanId, BigDecimal.ZERO, "PAY-008", "MPESA");
        });

        verifyNoInteractions(loanRepository);
    }

    /**
     * Test to ensure that a repayment with negative amount is rejected.
     */
    @Test
    void shouldRejectNegativeRepayment() {
        assertThrows(IllegalArgumentException.class, () -> {
            processRepaymentService.execute(loanId, new BigDecimal("-100.00"), "PAY-009", "MPESA");
        });

        verifyNoInteractions(loanRepository);
    }

    /**
     * Test to ensure that a repayment with null amount is rejected.
     */
    @Test
    void shouldRejectNullRepayment() {
        assertThrows(IllegalArgumentException.class, () -> {
            processRepaymentService.execute(loanId, null, "PAY-010", "MPESA");
        });

        verifyNoInteractions(loanRepository);
    }

    @Test
    void shouldPublishPaymentReceivedEvent() {
        BigDecimal amount = new BigDecimal("1000.00");
        String reference = "PAY-001";

        Installment installment = new Installment(
                repaymentSchedule,
                1,
                LocalDate.of(2026, 10, 17),
                new BigDecimal("3333.33"));

        when(loanRepository.findByIdForUpdate(loanId)).thenReturn(Optional.of(loan));
        when(repaymentRepository.existsByReference(reference)).thenReturn(false);
        when(loanFeeRepository.findOutstandingFees(loanId)).thenReturn(List.of());
        when(installmentRepository.findOutstandingInstallments(repaymentSchedule.getId()))
                .thenReturn(List.of(installment));
        when(repaymentRepository.save(any(Repayment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        processRepaymentService.execute(loanId, amount, reference, "MOBILE_MONEY");

        verify(eventPublisher).publishEvent(
                argThat((Object event) -> event instanceof NotificationEvent notificationEvent
                        && notificationEvent.eventType() == NotificationEventType.PAYMENT_RECEIVED
                        && Objects.equals(notificationEvent.loanId(), loan.getId())
                        && Objects.equals(notificationEvent.customerId(), loan.getCustomer().getId())
                        && notificationEvent.variables()
                                .get("amount")
                                .equals(amount)
                        && notificationEvent.variables()
                                .get("reference")
                                .equals(reference)));
    }

    @Test
    void shouldNotPublishPaymentReceivedEventWhenRepaymentFails() {

        assertThrows(
                IllegalArgumentException.class,
                () -> processRepaymentService.execute(
                        loanId,
                        BigDecimal.ZERO,
                        "PAY-001",
                        "MOBILE_MONEY"));

        verify(eventPublisher, never()).publishEvent(any());
    }
}
