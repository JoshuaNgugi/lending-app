package com.lending.app.product.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lending.app.product.domain.ProductFee;

public interface ProductFeeRepository extends JpaRepository<ProductFee, UUID> {

    List<ProductFee> findByProductId(UUID productId);
}
