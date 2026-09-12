package com.lending.app.customer.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lending.app.customer.domain.Customer;

public interface CustomerRepository extends JpaRepository<Customer, String> {

    Customer save(Customer customer);

    Optional<Customer> findById(UUID id);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);
}
