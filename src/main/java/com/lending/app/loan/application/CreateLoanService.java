package com.lending.app.loan.application;

import org.springframework.stereotype.Service;

import com.lending.app.customer.domain.Customer;
import com.lending.app.customer.domain.CustomerStatus;
import com.lending.app.customer.repository.CustomerRepository;
import com.lending.app.loan.api.CreateLoanRequest;
import com.lending.app.loan.domain.Loan;
import com.lending.app.loan.repository.LoanRepository;
import com.lending.app.product.domain.LoanProduct;
import com.lending.app.product.domain.ProductStatus;
import com.lending.app.product.repository.LoanProductRepository;
import com.lending.app.shared.exception.ResourceNotFoundException;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class CreateLoanService {

    private final LoanRepository loanRepository;
    private final CustomerRepository customerRepository;
    private final LoanProductRepository loanProductRepository;

    public CreateLoanService(LoanRepository loanRepository, CustomerRepository customerRepository,
            LoanProductRepository loanProductRepository) {

        this.loanRepository = loanRepository;
        this.customerRepository = customerRepository;
        this.loanProductRepository = loanProductRepository;
    }

    public Loan execute(CreateLoanRequest request) {
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + request.customerId()));

        LoanProduct product = loanProductRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Loan product not found: " + request.productId()));

        if (customer.getStatus() != CustomerStatus.ACTIVE) {
            throw new IllegalStateException("Customer is not active");
        }

        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new IllegalStateException("Loan product is not active");
        }

        Loan loan = new Loan(customer, product, request.principal());

        return loanRepository.save(loan);
    }

}
