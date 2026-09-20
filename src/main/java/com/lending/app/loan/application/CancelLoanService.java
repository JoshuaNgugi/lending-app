package com.lending.app.loan.application;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lending.app.loan.domain.Loan;
import com.lending.app.loan.persistence.LoanRepository;
import com.lending.app.shared.exception.ResourceNotFoundException;

@Service
@Transactional
public class CancelLoanService {

    private final LoanRepository loanRepository;

    public CancelLoanService(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    public Loan execute(UUID loanId) {
        Loan loan = loanRepository.findByIdForUpdate(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found: " + loanId));

        loan.cancel();

        return loanRepository.save(loan);
    }
}