package com.lending.app.customer.api;

import com.lending.app.customer.domain.CustomerSegment;

public record UpdateCustomerRequest(
        String firstName, String lastName, String phoneNumber, CustomerSegment segment) {

}
