package com.lending.app.product.domain;

import java.math.BigDecimal;
import java.util.UUID;

import com.lending.app.product.api.ProductFeeRequest;
import com.lending.app.product.exception.InvalidProductException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "product_fees")
public class ProductFee {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private LoanProduct product;

    @Enumerated(EnumType.STRING)
    @Column(name = "fee_type", nullable = false, length = 20)
    private FeeType feeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "calculation_type", nullable = false, length = 20)
    private FeeCalculationType calculationType;

    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal value;

    @Enumerated(EnumType.STRING)
    @Column(name = "application_timing", nullable = false, length = 30)
    private FeeApplicationTiming applicationTiming;

    @Column(name = "trigger_days")
    private Integer triggerDays;

    protected ProductFee() {
    }

    public ProductFee(
            FeeType feeType,
            FeeCalculationType calculationType,
            BigDecimal value,
            FeeApplicationTiming applicationTiming,
            Integer triggerDays) {

        validateInvariants(feeType, applicationTiming, triggerDays);

        this.feeType = feeType;
        this.calculationType = calculationType;
        this.value = value;
        this.applicationTiming = applicationTiming;
        this.triggerDays = triggerDays;
    }

    public UUID getId() {
        return id;
    }

    // Needed here for persistent relationship wiring rather than arbitrary business
    // state mutation
    public void setProduct(LoanProduct product) {
        this.product = product;
    }

    public LoanProduct getProduct() {
        return product;
    }

    public FeeType getFeeType() {
        return feeType;
    }

    public FeeCalculationType getCalculationType() {
        return calculationType;
    }

    public BigDecimal getValue() {
        return value;
    }

    public FeeApplicationTiming getApplicationTiming() {
        return applicationTiming;
    }

    public Integer getTriggerDays() {
        return triggerDays;
    }

    private void validateInvariants(
            FeeType feeType,
            FeeApplicationTiming applicationTiming,
            Integer triggerDays) {

        if (feeType == FeeType.LATE && applicationTiming != FeeApplicationTiming.AFTER_DUE_DATE) {
            throw new InvalidProductException("Late fee must be applied after due date");
        }

        if (feeType == FeeType.LATE && triggerDays == null) {
            throw new InvalidProductException("Late fee requires trigger days");
        }
    }

}
