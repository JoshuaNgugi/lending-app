package com.lending.app.repayment.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.lending.app.loan.domain.Loan;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "repayments")
public class Repayment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(nullable = false, length = 100)
    private String reference;

    @Column(nullable = false, length = 30)
    private String channel;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Repayment() {
    }

    public Repayment(
            Loan loan,
            BigDecimal amount,
            LocalDate paymentDate,
            String reference,
            String channel) {
        this.loan = loan;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.reference = reference;
        this.channel = channel;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public Loan getLoan() {
        return loan;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public String getReference() {
        return reference;
    }

    public String getChannel() {
        return channel;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
