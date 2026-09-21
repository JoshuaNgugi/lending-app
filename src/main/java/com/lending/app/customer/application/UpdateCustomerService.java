package com.lending.app.customer.application;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lending.app.customer.api.UpdateCustomerRequest;
import com.lending.app.customer.domain.Customer;
import com.lending.app.customer.persistence.CustomerRepository;
import com.lending.app.shared.exception.ResourceNotFoundException;

@Service
@Transactional
public class UpdateCustomerService {

    private final CustomerRepository customerRepository;

    public UpdateCustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer execute(UUID uuid, UpdateCustomerRequest request) {
        Customer customer = customerRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Customer with ID: " + uuid + " not found"));

        customer.updateDetails(
                request.firstName(),
                request.lastName(),
                request.phoneNumber(),
                request.segment());

        return customerRepository.save(customer);
    }

}
