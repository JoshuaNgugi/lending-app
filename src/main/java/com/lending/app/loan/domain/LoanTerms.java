package com.lending.app.loan.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.lending.app.product.domain.BillingMode;
import com.lending.app.product.domain.LoanStructure;
import com.lending.app.product.domain.TenureUnit;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "loan_terms")
public class LoanTerms {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loan_id", nullable = false, unique = true)
    private Loan loan;

    @Column(name = "tenure_value", nullable = false)
    private Integer tenureValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "tenure_unit", nullable = false, length = 10)
    private TenureUnit tenureUnit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LoanStructure structure;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_mode", nullable = false, length = 20)
    private BillingMode billingMode;

    @Column(name = "billing_day")
    private Integer billingDay;

    @Column(name = "grace_period_days", nullable = false)
    private Integer gracePeriodDays;

    @Column(name = "installment_count")
    private Integer installmentCount;

    @Column(name = "write_off_after_days")
    private Integer writeOffAfterDays;

    @OneToMany(mappedBy = "loanTerms", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LoanTermFee> fees = new ArrayList<>();

    protected LoanTerms() {
    }

    public LoanTerms(
            Loan loan,
            Integer tenureValue,
            TenureUnit tenureUnit,
            LoanStructure structure,
            BillingMode billingMode,
            Integer billingDay,
            Integer gracePeriodDays,
            Integer installmentCount) {
            this(loan, tenureValue, tenureUnit, structure, billingMode, billingDay, gracePeriodDays,
                installmentCount, null);
            }

            public LoanTerms(
                Loan loan,
                Integer tenureValue,
                TenureUnit tenureUnit,
                LoanStructure structure,
                BillingMode billingMode,
                Integer billingDay,
                Integer gracePeriodDays,
                Integer installmentCount,
                Integer writeOffAfterDays) {
        this.loan = loan;
        this.tenureValue = tenureValue;
        this.tenureUnit = tenureUnit;
        this.structure = structure;
        this.billingMode = billingMode;
        this.billingDay = billingDay;
        this.gracePeriodDays = gracePeriodDays;
        this.installmentCount = installmentCount;
        this.writeOffAfterDays = writeOffAfterDays;
    }

    public UUID getId() {
        return id;
    }

    public Integer getTenureValue() {
        return tenureValue;
    }

    public TenureUnit getTenureUnit() {
        return tenureUnit;
    }

    public LoanStructure getStructure() {
        return structure;
    }

    public BillingMode getBillingMode() {
        return billingMode;
    }

    public Integer getBillingDay() {
        return billingDay;
    }

    public Integer getGracePeriodDays() {
        return gracePeriodDays;
    }

    public Loan getLoan() {
        return loan;
    }

    public Integer getInstallmentCount() {
        return installmentCount;
    }

    public Integer getWriteOffAfterDays() {
        return writeOffAfterDays;
    }

    public List<LoanTermFee> getFees() {
        return fees;
    }

    public void addFee(LoanTermFee fee) {
        fees.add(fee);
    }
}
