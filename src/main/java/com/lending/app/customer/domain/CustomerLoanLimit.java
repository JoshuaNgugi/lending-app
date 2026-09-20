package com.lending.app.customer.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

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
@Table(name = "customer_loan_limits")
public class CustomerLoanLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal limitAmount;

    @Column
    private String currency;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column
    private String reason;

    @Column
    @Enumerated(EnumType.STRING)
    private CustomerLoanLimitStatus status;

    protected CustomerLoanLimit() {
    }

    public CustomerLoanLimit(
            Customer customer,
            BigDecimal limitAmount,
            String currency,
            LocalDate effectiveFrom,
            String reason) {

        this.customer = customer;
        this.limitAmount = limitAmount;
        this.currency = currency;
        this.effectiveFrom = effectiveFrom;
        this.reason = reason;
        this.status = CustomerLoanLimitStatus.ACTIVE;
    }

    public UUID getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public BigDecimal getLimitAmount() {
        return limitAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public LocalDate getEffectiveTo() {
        return effectiveTo;
    }

    public String getReason() {
        return reason;
    }

    public CustomerLoanLimitStatus getStatus() {
        return status;
    }

    public void expire() {
        if (status != CustomerLoanLimitStatus.ACTIVE) {
            throw new IllegalStateException("Only an active customer limit can expire.");
        }
        this.status = CustomerLoanLimitStatus.EXPIRED;
        this.effectiveTo = LocalDate.now();
    }
}
