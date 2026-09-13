package com.lending.app.product.api;

import java.util.UUID;

import com.lending.app.product.domain.BillingMode;
import com.lending.app.product.domain.LoanStructure;
import com.lending.app.product.domain.ProductStatus;
import com.lending.app.product.domain.TenureUnit;

public record LoanProductResponse(
        UUID id,
        String code,
        String name,
        String description,
        ProductStatus status,
        Integer tenureValue,
        TenureUnit tenureUnit,
        LoanStructure structure,
        BillingMode billingMode,
        Integer billingDay,
        Integer gracePeriodDays) {

}
