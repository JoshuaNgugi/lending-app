package com.lending.app.loan.api;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateLoanRequest(
        @NotNull UUID customerId,

        @NotNull UUID productId,

        @NotNull @Positive BigDecimal principal) {

}
