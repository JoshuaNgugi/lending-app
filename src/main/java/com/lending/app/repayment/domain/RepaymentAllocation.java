package com.lending.app.repayment.domain;

import java.math.BigDecimal;
import java.util.UUID;

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

@Entity
@Table(name = "repayment_allocations")
public class RepaymentAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "repayment_id", nullable = false)
    private Repayment repayment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "installment_id", nullable = false)
    private Installment installment;

    @Column(name = "fee_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal feeAmount;

    @Column(name = "principal_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal principalAmount;

    protected RepaymentAllocation() {
    }

    public RepaymentAllocation(
            Repayment repayment,
            Installment installment,
            BigDecimal feeAmount,
            BigDecimal principalAmount) {
        this.repayment = repayment;
        this.installment = installment;
        this.feeAmount = feeAmount;
        this.principalAmount = principalAmount;
    }
}