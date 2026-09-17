package com.lending.app.loan.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.lending.app.customer.domain.Customer;
import com.lending.app.product.domain.LoanProduct;
import com.lending.app.repayment_schedule.domain.RepaymentSchedule;

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "loans")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private LoanProduct product;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal principal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LoanStatus status;

    @Column(name = "origination_date")
    private LocalDate originationDate;

    @Column(name = "disbursement_date")
    private LocalDate disbursementDate;

    @Column(name = "maturity_date")
    private LocalDate maturityDate;

    @OneToOne(mappedBy = "loan", fetch = FetchType.LAZY)
    private LoanTerms loanTerms;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToOne(mappedBy = "loan", fetch = FetchType.LAZY)
    private RepaymentSchedule repaymentSchedule;

    protected Loan() {
    }

    public Loan(
            Customer customer,
            LoanProduct product,
            BigDecimal principal) {
        this.customer = customer;
        this.product = product;
        this.principal = principal;
        this.status = LoanStatus.CREATED;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public LoanProduct getProduct() {
        return product;
    }

    public BigDecimal getPrincipal() {
        return principal;
    }

    public LoanStatus getStatus() {
        return status;
    }

    public LocalDate getOriginationDate() {
        return originationDate;
    }

    public LocalDate getDisbursementDate() {
        return disbursementDate;
    }

    public LocalDate getMaturityDate() {
        return maturityDate;
    }

    public void setLoanTerms(LoanTerms loanTerms) {
        this.loanTerms = loanTerms;
    }

    public LoanTerms getLoanTerms() {
        return loanTerms;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setRepaymentSchedule(RepaymentSchedule repaymentSchedule) {
        this.repaymentSchedule = repaymentSchedule;
    }

    public RepaymentSchedule getRepaymentSchedule() {
        return repaymentSchedule;
    }

    public void disburse(LocalDate disbursementDate, LocalDate maturityDate) {

        if (status != LoanStatus.CREATED) {
            throw new IllegalStateException("Only CREATED loans can be disbursed");
        }

        this.disbursementDate = disbursementDate;
        this.originationDate = disbursementDate;
        this.maturityDate = maturityDate;
        this.status = LoanStatus.OPEN;
        this.updatedAt = Instant.now();
    }

    public void cancel() {

        if (status != LoanStatus.CREATED) {
            throw new IllegalStateException("Only CREATED loans can be cancelled");
        }

        this.status = LoanStatus.CANCELLED;
        this.updatedAt = Instant.now();
    }

    public void close() {
        if (status != LoanStatus.OPEN && status != LoanStatus.OVERDUE) {
            throw new IllegalStateException("Only open or overdue loans can be closed");
        }

        this.status = LoanStatus.CLOSED;
        this.updatedAt = Instant.now();
    }

    public void markOverdue() {
        if (status != LoanStatus.OPEN) {
            throw new IllegalStateException("Only open loans can be marked as overdue");
        }

        this.status = LoanStatus.OVERDUE;
        this.updatedAt = Instant.now();
    }
}
