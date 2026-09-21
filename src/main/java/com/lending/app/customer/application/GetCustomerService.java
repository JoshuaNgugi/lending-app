package com.lending.app.customer.application;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lending.app.customer.domain.Customer;
import com.lending.app.customer.persistence.CustomerRepository;
import com.lending.app.shared.exception.ResourceNotFoundException;

@Service
@Transactional(readOnly = true)
public class GetCustomerService {

    private final CustomerRepository customerRepository;

    public GetCustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer execute(UUID customerId) {

        return customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer with ID: " + customerId + " not found"));
    }

    public List<Customer> executeAll() {
        return customerRepository.findAll();
    }
}
