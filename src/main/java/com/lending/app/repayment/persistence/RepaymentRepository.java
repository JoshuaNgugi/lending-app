package com.lending.app.repayment.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lending.app.repayment.domain.Repayment;

public interface RepaymentRepository extends JpaRepository<Repayment, UUID> {

    boolean existsByReference(String reference);
}
