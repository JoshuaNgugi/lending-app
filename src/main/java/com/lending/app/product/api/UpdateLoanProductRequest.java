package com.lending.app.product.api;

import com.lending.app.product.domain.BillingMode;
import com.lending.app.product.domain.LoanStructure;
import com.lending.app.product.domain.TenureUnit;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record UpdateLoanProductRequest(
        String name,

        String description,

        @Positive Integer tenureValue,

        TenureUnit tenureUnit,

        LoanStructure structure,

        BillingMode billingMode,

        @Min(1) @Max(28) Integer billingDay,

        @PositiveOrZero Integer gracePeriodDays,

        @Positive Integer writeOffAfterDays

) {

}
