package com.lending.app.product.api;

import java.util.List;

import com.lending.app.product.domain.BillingMode;
import com.lending.app.product.domain.LoanStructure;
import com.lending.app.product.domain.TenureUnit;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateLoanProductRequest(

                @NotBlank String code,

                @NotBlank String name,

                String description,

                @NotNull @Positive Integer tenureValue,

                @NotNull TenureUnit tenureUnit,

                @NotNull LoanStructure structure,

                @NotNull BillingMode billingMode,

                @Min(1) @Max(28) Integer billingDay,

                @NotNull @PositiveOrZero Integer gracePeriodDays,

                List<@Valid ProductFeeRequest> fees,

                @Positive Integer installmentCount,

                @Positive Integer writeOffAfterDays) {
}
