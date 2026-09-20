package com.lending.app.customer.api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lending.app.customer.application.CreateCustomerService;
import com.lending.app.customer.application.CustomerLoanLimitDetails;
import com.lending.app.customer.application.CustomerLoanLimitService;
import com.lending.app.customer.application.GetCustomerService;
import com.lending.app.customer.application.UpdateCustomerService;
import com.lending.app.customer.domain.Customer;

import jakarta.validation.Valid;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {
    private final CreateCustomerService createCustomerService;
    private final GetCustomerService getCustomerService;
    private final UpdateCustomerService updateCustomerService;
    private final CustomerLoanLimitService customerLoanLimitService;

    public CustomerController(CreateCustomerService createCustomerService, GetCustomerService getCustomerService,
            UpdateCustomerService updateCustomerService, CustomerLoanLimitService customerLoanLimitService) {
        this.createCustomerService = createCustomerService;
        this.getCustomerService = getCustomerService;
        this.updateCustomerService = updateCustomerService;
        this.customerLoanLimitService = customerLoanLimitService;
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CreateCustomerRequest request) {
        Customer customer = createCustomerService.execute(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(customer));
    }

    @GetMapping("/{customerId}")
    public CustomerResponse get(
            @PathVariable("customerId") UUID customerId) {
        Customer customer = getCustomerService.execute(customerId);

        return toResponse(customer);
    }

    @PatchMapping("{customerId}")
    public ResponseEntity<CustomerResponse> patch(@PathVariable("customerId") UUID customerId,
            @RequestBody UpdateCustomerRequest request) {

        Customer customer = updateCustomerService.execute(customerId, request);
        return ResponseEntity.ok(toResponse(customer));
    }

    @PutMapping("/{customerId}/loan-limit")
    public CustomerLoanLimitResponse setLoanLimit(
            @PathVariable UUID customerId,
            @Valid @RequestBody SetCustomerLoanLimitRequest request) {

        CustomerLoanLimitDetails details = customerLoanLimitService.setLimit(
                customerId,
                request.limitAmount(),
                request.currency(),
                request.reason());

        return toCustomerLoanLimitResponse(details);
    }

    @GetMapping("/{customerId}/loan-limit")
    public CustomerLoanLimitResponse getLoanLimit(@PathVariable UUID customerId) {
        return toCustomerLoanLimitResponse(customerLoanLimitService.getLimit(customerId));
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

    private CustomerLoanLimitResponse toCustomerLoanLimitResponse(CustomerLoanLimitDetails details) {
        return new CustomerLoanLimitResponse(
                details.limit().getLimitAmount(),
                details.limit().getCurrency(),
                details.availableAmount(),
                details.limit().getReason(),
                details.limit().getEffectiveFrom(),
                details.limit().getStatus().name());
    }

}
