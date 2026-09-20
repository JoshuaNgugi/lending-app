package com.lending.app.customer.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lending.app.customer.domain.Customer;

import jakarta.persistence.LockModeType;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Customer c WHERE c.id = :customerId")
    Optional<Customer> findByIdForUpdate(@Param("customerId") UUID customerId);

    Optional<Customer> findById(UUID id);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);
}
