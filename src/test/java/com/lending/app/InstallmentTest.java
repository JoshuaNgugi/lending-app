package com.lending.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.lending.app.repayment_schedule.domain.Installment;
import com.lending.app.repayment_schedule.domain.InstallmentStatus;

public class InstallmentTest {
    private Installment installment;

    @BeforeEach
    void setUp() {
        installment = new Installment(null, 1, LocalDate.of(2026, 10, 17), new BigDecimal("3333.33"));
    }

    @Test
    void shouldCreatePendingInstallment() {
        assertEquals(1, installment.getInstallmentNumber());
        assertEquals(LocalDate.of(2026, 10, 17), installment.getDueDate());
        assertEquals(new BigDecimal("3333.33"), installment.getPrincipalDue());
        assertEquals(BigDecimal.ZERO, installment.getPrincipalPaid());
        assertEquals(InstallmentStatus.PENDING, installment.getStatus());
    }

    @Test
    void shouldCalculateOutstandingPrincipal() {
        assertEquals(new BigDecimal("3333.33"), installment.getOutstandingPrincipal());
    }

    @Test
    void shouldPartiallyAllocatePrincipal() {
        installment.allocatePrincipal(new BigDecimal("1000.00"));

        assertEquals(new BigDecimal("1000.00"), installment.getPrincipalPaid());

        assertEquals(new BigDecimal("2333.33"), installment.getOutstandingPrincipal());

        assertEquals(InstallmentStatus.PARTIALLY_PAID, installment.getStatus());
    }

    @Test
    void shouldFullyAllocatePrincipal() {
        installment.allocatePrincipal(new BigDecimal("3333.33"));

        assertEquals(new BigDecimal("3333.33"), installment.getPrincipalPaid());

        assertEquals(0, installment.getOutstandingPrincipal().compareTo(BigDecimal.ZERO));

        assertEquals(InstallmentStatus.PAID, installment.getStatus());
    }

    @Test
    void shouldRejectZeroAllocation() {
        assertThrows(IllegalArgumentException.class, () -> installment.allocatePrincipal(BigDecimal.ZERO));
    }

    @Test
    void shouldRejectNegativeAllocation() {
        assertThrows(IllegalArgumentException.class, () -> installment.allocatePrincipal(new BigDecimal("-100.00")));
    }

    @Test
    void shouldRejectAllocationGreaterThanOutstanding() {
        assertThrows(IllegalArgumentException.class, () -> installment.allocatePrincipal(new BigDecimal("3333.34")));
    }
}
