package com.lending.app.loan.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lending.app.loan.domain.LoanFee;

public interface LoanFeeRepository extends JpaRepository<LoanFee, UUID> {

}
