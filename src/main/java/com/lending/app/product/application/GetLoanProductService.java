package com.lending.app.product.application;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lending.app.product.domain.LoanProduct;
import com.lending.app.product.exception.LoanProductNotFoundException;
import com.lending.app.product.persistence.LoanProductRepository;

@Service
@Transactional(readOnly = true)
public class GetLoanProductService {
    private final LoanProductRepository loanProductRepository;

    public GetLoanProductService(LoanProductRepository loanProductRepository) {
        this.loanProductRepository = loanProductRepository;
    }

    public LoanProduct execute(UUID productId) {
        return loanProductRepository.findById(productId).orElseThrow(
                () -> new LoanProductNotFoundException("Loan product with id: " + productId + " not found"));
    }

    public List<LoanProduct> executeAll() {
        return loanProductRepository.findAll();
    }
}