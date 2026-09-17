package com.lending.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.lending.app.loan.domain.LoanFee;
import com.lending.app.product.domain.FeeType;

public class LoanFeeTest {
    private LoanFee loanFee;

    @BeforeEach
    void setUp() {
        loanFee = new LoanFee(null, FeeType.SERVICE, new BigDecimal("500.00"),
                LocalDate.of(2026, 9, 17), null, null,
                "Original service fee");
    }

    @Test
    void shouldCreateFeeWithNoPayment() {
        assertEquals(new BigDecimal("500.00"), loanFee.getAmount());

        assertEquals(BigDecimal.ZERO, loanFee.getAmountPaid());

        assertEquals(new BigDecimal("500.00"), loanFee.getOutstandingAmount());
    }

    @Test
    void shouldPartiallyAllocatePayment() {
        loanFee.allocatePayment(new BigDecimal("200.00"));

        assertEquals(new BigDecimal("200.00"), loanFee.getAmountPaid());

        assertEquals(new BigDecimal("300.00"), loanFee.getOutstandingAmount());
    }

    @Test
    void shouldFullyAllocatePayment() {
        loanFee.allocatePayment(new BigDecimal("500.00"));

        assertEquals(new BigDecimal("500.00"), loanFee.getAmountPaid());

        assertEquals(0, loanFee.getOutstandingAmount().compareTo(BigDecimal.ZERO));
    }

    @Test
    void shouldRejectZeroPayment() {
        assertThrows(IllegalArgumentException.class, () -> loanFee.allocatePayment(BigDecimal.ZERO));
    }

    @Test
    void shouldRejectNegativePayment() {
        assertThrows(IllegalArgumentException.class, () -> loanFee.allocatePayment(new BigDecimal("-100.00")));
    }

    @Test
    void shouldRejectPaymentGreaterThanOutstanding() {
        assertThrows(IllegalArgumentException.class, () -> loanFee.allocatePayment(new BigDecimal("500.01")));
    }
}
