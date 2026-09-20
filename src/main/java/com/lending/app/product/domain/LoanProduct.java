package com.lending.app.product.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "loan_products", uniqueConstraints = {
        @UniqueConstraint(name = "uk_loan_product_code", columnNames = "code")
})
public class LoanProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 20)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status;

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

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected LoanProduct() {
    }

    public LoanProduct(
            String code,
            String name,
            String description,
            Integer tenureValue,
            TenureUnit tenureUnit,
            LoanStructure structure,
            BillingMode billingMode,
            Integer billingDay,
            Integer gracePeriodDays,
            Integer installmentCount) {
            this(code, name, description, tenureValue, tenureUnit, structure, billingMode, billingDay,
                gracePeriodDays, installmentCount, null);
            }

            public LoanProduct(
                String code,
                String name,
                String description,
                Integer tenureValue,
                TenureUnit tenureUnit,
                LoanStructure structure,
                BillingMode billingMode,
                Integer billingDay,
                Integer gracePeriodDays,
                Integer installmentCount,
                Integer writeOffAfterDays) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.tenureValue = tenureValue;
        this.tenureUnit = tenureUnit;
        this.structure = structure;
        this.billingMode = billingMode;
        this.billingDay = billingDay;
        this.gracePeriodDays = gracePeriodDays;
        this.status = ProductStatus.ACTIVE;
        this.installmentCount = installmentCount;
        this.writeOffAfterDays = writeOffAfterDays;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ProductStatus getStatus() {
        return status;
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

    public Integer getInstallmentCount() {
        return installmentCount;
    }

    public Integer getWriteOffAfterDays() {
        return writeOffAfterDays;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void update(
            String name,
            String description,
            Integer tenureValue,
            TenureUnit tenureUnit,
            LoanStructure structure,
            BillingMode billingMode,
            Integer billingDay,
            Integer gracePeriodDays,
            Integer writeOffAfterDays) {
        this.name = name;
        this.description = description;
        this.tenureValue = tenureValue;
        this.tenureUnit = tenureUnit;
        this.structure = structure;
        this.billingMode = billingMode;
        this.billingDay = billingDay;
        this.gracePeriodDays = gracePeriodDays;
        this.writeOffAfterDays = writeOffAfterDays;
        this.updatedAt = Instant.now();
    }

    public void deactivate() {
        this.status = ProductStatus.INACTIVE;
        this.updatedAt = Instant.now();
    }
}
