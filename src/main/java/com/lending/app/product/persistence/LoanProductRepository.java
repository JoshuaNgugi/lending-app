package com.lending.app.product.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lending.app.product.domain.LoanProduct;

public interface LoanProductRepository extends JpaRepository<LoanProduct, UUID> {

    boolean existsByCodeIgnoreCase(String code);
}
