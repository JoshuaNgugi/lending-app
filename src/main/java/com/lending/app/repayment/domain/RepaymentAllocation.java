package com.lending.app.repayment.domain;

import java.math.BigDecimal;
import java.util.UUID;

import com.lending.app.loan.domain.LoanFee;
import com.lending.app.repayment_schedule.domain.Installment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * A repayment says "customer paid KES 4,000"
 * 
 * A Repayment Allocation says "Of that KES 4,000 this much was applied to this
 * obligation"
 * 
 * Every repayment allocation points to exacty on thing: either a Loan Fee or an
 * Installment
 */
@Entity
@Table(name = "repayment_allocations")
public class RepaymentAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "repayment_id", nullable = false)
    private Repayment repayment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "installment_id")
    private Installment installment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_fee_id")
    private LoanFee loanFee;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    protected RepaymentAllocation() {
    }

    public RepaymentAllocation(
            Repayment repayment,
            Installment installment,
            LoanFee loanFee,
            BigDecimal amount) {
        this.repayment = repayment;
        this.installment = installment;
        this.loanFee = loanFee;
        this.amount = amount;
    }

    public UUID getId() {
        return id;
    }

    public Repayment getRepayment() {
        return repayment;
    }

    public Installment getInstallment() {
        return installment;
    }

    public LoanFee getLoanFee() {
        return loanFee;
    }

    public BigDecimal getAmount() {
        return amount;
    }

}