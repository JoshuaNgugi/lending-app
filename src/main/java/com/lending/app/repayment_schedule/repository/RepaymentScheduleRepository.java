package com.lending.app.repayment_schedule.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lending.app.repayment_schedule.domain.RepaymentSchedule;

public interface RepaymentScheduleRepository extends JpaRepository<RepaymentSchedule, UUID> {

    boolean existsByLoanId(UUID loanId);
}
