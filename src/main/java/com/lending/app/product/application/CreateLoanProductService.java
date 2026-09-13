package com.lending.app.product.application;

import org.springframework.stereotype.Service;

import com.lending.app.product.api.CreateLoanProductRequest;
import com.lending.app.product.api.ProductFeeRequest;
import com.lending.app.product.domain.LoanProduct;
import com.lending.app.product.domain.ProductFee;
import com.lending.app.product.exception.ProductAlreadyExistsException;
import com.lending.app.product.repository.LoanProductRepository;
import com.lending.app.product.repository.ProductFeeRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class CreateLoanProductService {

    private final LoanProductRepository productRepository;
    private final ProductFeeRepository feeRepository;

    public CreateLoanProductService(
            LoanProductRepository productRepository,
            ProductFeeRepository feeRepository) {
        this.productRepository = productRepository;
        this.feeRepository = feeRepository;
    }

    public LoanProduct execute(CreateLoanProductRequest request) {

        if (productRepository.existsByCodeIgnoreCase(request.code())) {
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
                request.gracePeriodDays());

        LoanProduct saved = productRepository.save(product);

        if (request.fees() != null) {
            for (ProductFeeRequest feeRequest : request.fees()) {
                ProductFee fee = new ProductFee(
                        feeRequest.feeType(),
                        feeRequest.calculationType(),
                        feeRequest.value(),
                        feeRequest.applicationTiming(),
                        feeRequest.triggerDays());

                fee.setProduct(saved);
                feeRepository.save(fee);
            }
        }

        return saved;
    }

}
