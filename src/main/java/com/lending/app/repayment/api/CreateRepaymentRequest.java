package com.lending.app.repayment.api;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateRepaymentRequest(

        @NotNull @Positive BigDecimal amount,

        @NotBlank String reference,

        @NotBlank String channel) {
}
