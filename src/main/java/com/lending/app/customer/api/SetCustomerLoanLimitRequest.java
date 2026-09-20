package com.lending.app.customer.api;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SetCustomerLoanLimitRequest(
        @NotNull @Positive BigDecimal limitAmount,
        @NotBlank String currency,
        @NotBlank String reason) {
}