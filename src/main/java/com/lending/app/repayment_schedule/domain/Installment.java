package com.lending.app.repayment_schedule.domain;

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
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "installments", uniqueConstraints = {
        @UniqueConstraint(name = "uq_installment_schedule_number", columnNames = { "schedule_id",
                "installment_number" })
})
public class Installment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "schedule_id", nullable = false)
    private RepaymentSchedule schedule;

    @Column(name = "installment_number", nullable = false)
    private Integer installmentNumber;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "principal_due", nullable = false, precision = 19, scale = 2)
    private BigDecimal principalDue;

    @Column(name = "principal_paid", nullable = false, precision = 19, scale = 2)
    private BigDecimal principalPaid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InstallmentStatus status;

    protected Installment() {
    }

    public Installment(
            RepaymentSchedule schedule,
            Integer installmentNumber,
            LocalDate dueDate,
            BigDecimal principalDue) {
        this.schedule = schedule;
        this.installmentNumber = installmentNumber;
        this.dueDate = dueDate;
        this.principalDue = principalDue;
        this.principalPaid = BigDecimal.ZERO;
        this.status = InstallmentStatus.PENDING;
    }

    public UUID getId() {
        return id;
    }

    public Integer getInstallmentNumber() {
        return installmentNumber;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public BigDecimal getPrincipalDue() {
        return principalDue;
    }

    public BigDecimal getPrincipalPaid() {
        return principalPaid;
    }

    public InstallmentStatus getStatus() {
        return status;
    }

    public RepaymentSchedule getSchedule() {
        return schedule;
    }

    public BigDecimal getOutstandingPrincipal() {
        return principalDue.subtract(principalPaid);
    }

    public void allocatePrincipal(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        if (amount.compareTo(getOutstandingPrincipal()) > 0) {
            throw new IllegalArgumentException("Amount exceeds outstanding principal");
        }
        principalPaid = principalPaid.add(amount);

        if (principalPaid.compareTo(principalDue) == 0) {
            status = InstallmentStatus.PAID;
        } else {
            status = InstallmentStatus.PARTIALLY_PAID;
        }
    }

    public void markOverdue() {

        if (status == InstallmentStatus.PAID) {
            throw new IllegalStateException("Paid installments cannot be marked overdue");
        }

        this.status = InstallmentStatus.OVERDUE;
    }
}
