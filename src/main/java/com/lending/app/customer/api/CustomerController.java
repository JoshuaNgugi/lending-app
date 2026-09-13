package com.lending.app.customer.api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lending.app.customer.application.CreateCustomerService;
import com.lending.app.customer.application.GetCustomerService;
import com.lending.app.customer.domain.Customer;

import jakarta.validation.Valid;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {
    private final CreateCustomerService createCustomerService;
    private final GetCustomerService getCustomerService;

    public CustomerController(CreateCustomerService createCustomerService, GetCustomerService getCustomerService) {
        this.createCustomerService = createCustomerService;
        this.getCustomerService = getCustomerService;
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CreateCustomerRequest request) {
        Customer customer = createCustomerService.execute(request);

        CustomerResponse response = new CustomerResponse(
                customer.getId(), customer.getFirstName(), customer.getLastName(), customer.getEmail(),
                customer.getPhoneNumber(), customer.getStatus(), customer.getSegment(),
                customer.getCreatedAt(), customer.getUpdatedAt());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{customerId}")
    public CustomerResponse get(
            @PathVariable("customerId") UUID customerId) {
        Customer customer = getCustomerService.execute(customerId);

        return toResponse(customer);
    }

    private CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getPhoneNumber(),
                customer.getStatus(),
                customer.getSegment(),
                customer.getCreatedAt(),
                customer.getUpdatedAt());
    }

}
