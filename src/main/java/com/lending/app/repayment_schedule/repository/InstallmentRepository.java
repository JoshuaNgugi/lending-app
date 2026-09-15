package com.lending.app.repayment_schedule.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lending.app.repayment_schedule.domain.Installment;

public interface InstallmentRepository extends JpaRepository<Installment, UUID> {

    List<Installment> findByScheduleIdOrderByInstallmentNumber(UUID scheduleId);
}
