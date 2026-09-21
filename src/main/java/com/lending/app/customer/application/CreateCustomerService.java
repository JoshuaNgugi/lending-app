package com.lending.app.customer.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lending.app.customer.api.CreateCustomerRequest;
import com.lending.app.customer.domain.Customer;
import com.lending.app.customer.persistence.CustomerRepository;
import com.lending.app.shared.exception.ResourceAlreadyExistsException;

@Service
@Transactional
public class CreateCustomerService {
    private final CustomerRepository customerRepository;

    public CreateCustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer execute(CreateCustomerRequest request) {

        if (customerRepository.existsByEmail(request.email())) {
            throw new ResourceAlreadyExistsException(
                    "Customer with the provided email already exists");
        }

        if (customerRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new ResourceAlreadyExistsException(
                    "Customer with the provided phone number already exists");
        }

        Customer customer = new Customer(
                request.firstName(),
                request.lastName(),
                request.email(),
                request.phoneNumber(),
                request.segment());

        return customerRepository.save(customer);
    }
}
