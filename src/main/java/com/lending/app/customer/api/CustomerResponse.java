package com.lending.app.customer.api;

import java.time.Instant;
import java.util.UUID;

import com.lending.app.customer.domain.CustomerSegment;
import com.lending.app.customer.domain.CustomerStatus;

public record CustomerResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        CustomerStatus status,
        CustomerSegment segment,
        Instant createdAt,
        Instant updatedAt) {

}
