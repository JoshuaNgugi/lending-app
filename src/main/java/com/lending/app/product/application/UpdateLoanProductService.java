package com.lending.app.product.application;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lending.app.product.api.UpdateLoanProductRequest;
import com.lending.app.product.domain.BillingMode;
import com.lending.app.product.domain.LoanProduct;
import com.lending.app.product.exception.LoanProductNotFoundException;
import com.lending.app.product.persistence.LoanProductRepository;

@Service
@Transactional
public class UpdateLoanProductService {
    private final LoanProductRepository loanProductRepository;

    public UpdateLoanProductService(
            LoanProductRepository loanProductRepository) {
        this.loanProductRepository = loanProductRepository;
    }

    public LoanProduct execute(
            UUID productId,
            UpdateLoanProductRequest request) {

        LoanProduct product = loanProductRepository.findById(productId)
                .orElseThrow(() -> new LoanProductNotFoundException("Loan product not found: " + productId));

        validate(request);

        product.update(
                request.name(),
                request.description(),
                request.tenureValue(),
                request.tenureUnit(),
                request.structure(),
                request.billingMode(),
                request.billingDay(),
                request.gracePeriodDays(),
                request.writeOffAfterDays());

        return loanProductRepository.save(product);
    }

    private void validate(UpdateLoanProductRequest request) {

        if (request.billingMode() == BillingMode.CONSOLIDATED
                && request.billingDay() == null) {
            throw new IllegalArgumentException(
                    "Billing day is required for consolidated billing");
        }

        if (request.billingMode() == BillingMode.INDIVIDUAL
                && request.billingDay() != null) {
            throw new IllegalArgumentException(
                    "Billing day must not be provided for individual billing");
        }
    }
}
