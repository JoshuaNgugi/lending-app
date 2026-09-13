package com.lending.app.customer.api;

import com.lending.app.customer.domain.CustomerSegment;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCustomerRequest(
        @NotBlank String firstName,

        @NotBlank String lastName,

        @Email @NotBlank String email,

        @NotBlank String phoneNumber,

        @NotNull CustomerSegment segment) {

}
