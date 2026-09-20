package com.lending.app.loan.application;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lending.app.loan.domain.Loan;
import com.lending.app.loan.persistence.LoanRepository;
import com.lending.app.shared.exception.ResourceNotFoundException;

@Service
@Transactional(readOnly = true)
public class GetLoanService {

    private final LoanRepository loanRepository;

    public GetLoanService(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    public Loan execute(UUID loanId) {
        return loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found: " + loanId));
    }
}