package com.lending.app.product.application;

import org.springframework.stereotype.Service;

import com.lending.app.product.api.CreateLoanProductRequest;
import com.lending.app.product.api.ProductFeeRequest;
import com.lending.app.product.domain.BillingMode;
import com.lending.app.product.domain.FeeApplicationTiming;
import com.lending.app.product.domain.FeeType;
import com.lending.app.product.domain.LoanProduct;
import com.lending.app.product.domain.LoanStructure;
import com.lending.app.product.domain.ProductFee;
import com.lending.app.product.exception.ProductAlreadyExistsException;
import com.lending.app.product.persistence.LoanProductRepository;
import com.lending.app.product.persistence.ProductFeeRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class CreateLoanProductService {

    private final LoanProductRepository loanProductRepository;
    private final ProductFeeRepository productFeeRepository;

    public CreateLoanProductService(
            LoanProductRepository loanProductRepository,
            ProductFeeRepository productFeeRepository) {
        this.loanProductRepository = loanProductRepository;
        this.productFeeRepository = productFeeRepository;
    }

    public LoanProduct execute(CreateLoanProductRequest request) {

        validate(request);

        if (loanProductRepository.existsByCodeIgnoreCase(request.code())) {
            throw new ProductAlreadyExistsException("Product with code already exists");
        }

        LoanProduct product = new LoanProduct(
                request.code(),
                request.name(),
                request.description(),
                request.tenureValue(),
                request.tenureUnit(),
                request.structure(),
                request.billingMode(),
                request.billingDay(),
                request.gracePeriodDays(),
                request.installmentCount(),
                request.writeOffAfterDays());

        LoanProduct saved = loanProductRepository.save(product);

        if (request.fees() != null) {
            for (ProductFeeRequest feeRequest : request.fees()) {
                ProductFee fee = new ProductFee(
                        feeRequest.feeType(),
                        feeRequest.calculationType(),
                        feeRequest.value(),
                        feeRequest.applicationTiming(),
                        feeRequest.triggerDays());

                fee.setProduct(saved);
                productFeeRepository.save(fee);
            }
        }

        return saved;
    }

    private void validate(CreateLoanProductRequest request) {

        if (request.billingMode() == BillingMode.CONSOLIDATED
                && request.billingDay() == null) {
            throw new IllegalArgumentException("Billing day is required for consolidated billing");
        }

        if (request.billingMode() == BillingMode.INDIVIDUAL
                && request.billingDay() != null) {
            throw new IllegalArgumentException("Billing day must not be provided for individual billing");
        }

        if (request.structure() == LoanStructure.INSTALLMENT
                && request.installmentCount() == null) {

            throw new IllegalArgumentException("Installment count is required for installment loans");
        }

        if (request.structure() == LoanStructure.LUMP_SUM
                && request.installmentCount() != null) {

            throw new IllegalArgumentException("Installment count must not be provided for lump-sum loans");
        }

        if (request.fees() == null) {
            return;
        }

        for (ProductFeeRequest fee : request.fees()) {

            if (fee.feeType() == FeeType.LATE
                    && fee.applicationTiming() != FeeApplicationTiming.AFTER_DUE_DATE) {
                throw new IllegalArgumentException(
                        "Late fees must be applied after the due date");
            }

            if (fee.feeType() == FeeType.LATE
                    && fee.triggerDays() == null) {
                throw new IllegalArgumentException(
                        "Late fees require trigger days");
            }
        }
    }
}
