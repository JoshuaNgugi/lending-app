package com.lending.app.loan.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lending.app.loan.domain.LoanTerms;

public interface LoanTermsRepository extends JpaRepository<LoanTerms, UUID> {

}
