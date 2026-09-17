package com.lending.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.lending.app.loan.domain.Loan;
import com.lending.app.loan.domain.LoanStatus;

public class LoanTest {
    private Loan loan;

    @BeforeEach
    void setUp() {
        loan = new Loan(null, null, new BigDecimal("1000.00"));
    }

    @Test
    void shouldCreateLoanWithCreatedStatus() {
        assertEquals(LoanStatus.CREATED, loan.getStatus());
        assertEquals(new BigDecimal("1000.00"), loan.getPrincipal());
        assertNotNull(loan.getCreatedAt());
        assertNotNull(loan.getUpdatedAt());
    }

    @Test
    void shouldDisburseCreatedLoan() {
        LocalDate disbursementDate = LocalDate.of(2026, 9, 17);
        LocalDate maturityDate = LocalDate.of(2027, 12, 17);

        loan.disburse(disbursementDate, maturityDate);

        assertEquals(LoanStatus.OPEN, loan.getStatus());
        assertEquals(disbursementDate, loan.getDisbursementDate());
        assertEquals(disbursementDate, loan.getOriginationDate());
        assertEquals(maturityDate, loan.getMaturityDate());
    }

    @Test
    void shouldNotDisburseLoanMoreThanOnce() {
        LocalDate disbursementDate = LocalDate.of(2026, 9, 17);
        LocalDate maturityDate = LocalDate.of(2026, 12, 17);

        loan.disburse(disbursementDate, maturityDate);

        assertThrows(IllegalStateException.class, () -> loan.disburse(disbursementDate, maturityDate));
    }

    @Test
    void shouldCancelCreatedLoan() {
        loan.cancel();

        assertEquals(LoanStatus.CANCELLED, loan.getStatus());
    }

    @Test
    void shouldNotCancelDisbursedLoan() {
        loan.disburse(
                LocalDate.of(2026, 9, 17),
                LocalDate.of(2026, 12, 17));

        assertThrows(IllegalStateException.class, () -> loan.cancel());
    }

    @Test
    void shouldCloseOpenLoan() {
        loan.disburse(
                LocalDate.of(2026, 9, 17),
                LocalDate.of(2026, 12, 17));

        loan.close();

        assertEquals(LoanStatus.CLOSED, loan.getStatus());
    }

    @Test
    void shouldNotCloseCreatedLoan() {
        assertThrows(IllegalStateException.class, () -> loan.close());
    }
}
