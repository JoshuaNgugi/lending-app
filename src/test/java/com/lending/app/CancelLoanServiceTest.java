package com.lending.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lending.app.loan.application.CancelLoanService;
import com.lending.app.loan.domain.Loan;
import com.lending.app.loan.domain.LoanStatus;
import com.lending.app.loan.persistence.LoanRepository;
import com.lending.app.shared.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class CancelLoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @InjectMocks
    private CancelLoanService cancelLoanService;

    @Test
    void cancelsCreatedLoanUsingLockedLookup() {
        UUID loanId = UUID.randomUUID();
        Loan loan = new Loan(null, null, new BigDecimal("1000.00"));
        when(loanRepository.findByIdForUpdate(loanId)).thenReturn(Optional.of(loan));
        when(loanRepository.save(loan)).thenReturn(loan);

        Loan cancelledLoan = cancelLoanService.execute(loanId);

        assertEquals(LoanStatus.CANCELLED, cancelledLoan.getStatus());
        verify(loanRepository).findByIdForUpdate(loanId);
        verify(loanRepository).save(loan);
    }

    @Test
    void rejectsCancellationWhenLoanIsAlreadyDisbursed() {
        UUID loanId = UUID.randomUUID();
        Loan loan = new Loan(null, null, new BigDecimal("1000.00"));
        loan.disburse(java.time.LocalDate.of(2026, 9, 20), java.time.LocalDate.of(2026, 12, 20));
        when(loanRepository.findByIdForUpdate(loanId)).thenReturn(Optional.of(loan));

        assertThrows(IllegalStateException.class, () -> cancelLoanService.execute(loanId));
    }

    @Test
    void rejectsCancellationWhenLoanDoesNotExist() {
        UUID loanId = UUID.randomUUID();
        when(loanRepository.findByIdForUpdate(loanId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cancelLoanService.execute(loanId));
    }
}
