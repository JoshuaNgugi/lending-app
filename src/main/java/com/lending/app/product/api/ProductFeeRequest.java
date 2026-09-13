package com.lending.app.product.api;

import java.math.BigDecimal;

import com.lending.app.product.domain.FeeApplicationTiming;
import com.lending.app.product.domain.FeeCalculationType;
import com.lending.app.product.domain.FeeType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record ProductFeeRequest(

        @NotNull FeeType feeType,

        @NotNull FeeCalculationType calculationType,

        @NotNull @Positive BigDecimal value,

        @NotNull FeeApplicationTiming applicationTiming,

        @PositiveOrZero Integer triggerDays) {
}
