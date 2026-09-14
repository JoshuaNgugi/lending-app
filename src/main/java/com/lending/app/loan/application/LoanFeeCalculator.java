package com.lending.app.loan.application;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

import com.lending.app.loan.domain.LoanTermFee;
import com.lending.app.product.domain.FeeCalculationType;

@Component
public class LoanFeeCalculator {

    public BigDecimal calculate(LoanTermFee fee, BigDecimal principal) {

        if (fee.getCalculationType() == FeeCalculationType.FIXED) {
            return fee.getValue();
        }

        return principal
                .multiply(fee.getValue())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}
