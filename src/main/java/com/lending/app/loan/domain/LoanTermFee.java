package com.lending.app.loan.domain;

import java.math.BigDecimal;
import java.util.UUID;

import com.lending.app.product.domain.FeeApplicationTiming;
import com.lending.app.product.domain.FeeCalculationType;
import com.lending.app.product.domain.FeeType;

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
@Table(name = "loan_term_fees")
public class LoanTermFee {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loan_terms_id", nullable = false)
    private LoanTerms loanTerms;

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

    protected LoanTermFee() {
    }

    public LoanTermFee(
            LoanTerms loanTerms,
            FeeType feeType,
            FeeCalculationType calculationType,
            BigDecimal value,
            FeeApplicationTiming applicationTiming,
            Integer triggerDays) {
        this.loanTerms = loanTerms;
        this.feeType = feeType;
        this.calculationType = calculationType;
        this.value = value;
        this.applicationTiming = applicationTiming;
        this.triggerDays = triggerDays;
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
}
