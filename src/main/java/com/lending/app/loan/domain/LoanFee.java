package com.lending.app.loan.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

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
@Table(name = "loan_fees")
public class LoanFee {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Enumerated(EnumType.STRING)
    @Column(name = "fee_type", nullable = false, length = 20)
    private FeeType feeType;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "applied_date", nullable = false)
    private LocalDate appliedDate;

    @Column(name = "reference_date")
    private LocalDate referenceDate;

    @Column(length = 100)
    private String reference;

    @Column(length = 255)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected LoanFee() {
    }

    public LoanFee(
            Loan loan,
            FeeType feeType,
            BigDecimal amount,
            LocalDate appliedDate,
            LocalDate referenceDate,
            String reference,
            String reason) {
        this.loan = loan;
        this.feeType = feeType;
        this.amount = amount;
        this.appliedDate = appliedDate;
        this.referenceDate = referenceDate;
        this.reference = reference;
        this.reason = reason;
        this.createdAt = Instant.now();
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public FeeType getFeeType() {
        return feeType;
    }
}
