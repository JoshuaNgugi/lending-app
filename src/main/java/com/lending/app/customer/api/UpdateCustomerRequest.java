package com.lending.app.customer.api;

import com.lending.app.customer.domain.CustomerSegment;

import jakarta.validation.constraints.Size;

public record UpdateCustomerRequest(
        @Size(min = 1, max = 100) String firstName,
        @Size(min = 1, max = 100) String lastName,
        @Size(min = 1, max = 30) String phoneNumber,
        CustomerSegment segment) {

}
