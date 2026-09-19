package com.lending.app.repayment_schedule.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lending.app.repayment_schedule.domain.Installment;

public interface InstallmentRepository extends JpaRepository<Installment, UUID> {

    List<Installment> findByScheduleIdOrderByInstallmentNumber(UUID scheduleId);

    @Query("""
            SELECT i FROM Installment i
            WHERE i.schedule.id = :scheduleId
            AND i.principalPaid < i.principalDue
            ORDER BY i.dueDate ASC, i.installmentNumber ASC
            """)
    List<Installment> findOutstandingInstallments(@Param("scheduleId") UUID scheduleId);

    @Query("""
            SELECT i FROM Installment i
            WHERE i.dueDate = :dueDate
            AND i.principalPaid < i.principalDue
            """)
    List<Installment> findOutstandingInstallmentDueOn(@Param("dueDate") LocalDate dueDate);
}
