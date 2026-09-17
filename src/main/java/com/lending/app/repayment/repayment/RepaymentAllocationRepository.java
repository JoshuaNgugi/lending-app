package com.lending.app.repayment.repayment;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lending.app.repayment.domain.RepaymentAllocation;

public interface RepaymentAllocationRepository extends JpaRepository<RepaymentAllocation, UUID> {

    List<RepaymentAllocation> findByRepaymentId(UUID repaymentId);
}
