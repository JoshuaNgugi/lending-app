package com.lending.app.product.application;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lending.app.product.domain.LoanProduct;
import com.lending.app.product.exception.LoanProductNotFoundException;
import com.lending.app.product.persistence.LoanProductRepository;

@Service
@Transactional
public class DeactivateLoanProductService {

    private final LoanProductRepository loanProductRepository;

    public DeactivateLoanProductService(LoanProductRepository loanProductRepository) {
        this.loanProductRepository = loanProductRepository;
    }

    public LoanProduct execute(UUID productId) {
        LoanProduct product = loanProductRepository.findById(productId)
                .orElseThrow(() -> new LoanProductNotFoundException("Loan product not found: " + productId));

        product.deactivate();
        return loanProductRepository.save(product);
    }
}
