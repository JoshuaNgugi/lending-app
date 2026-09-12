package com.lending.app.customer.domain;

import java.time.Instant;
import java.util.UUID;

public class Customer {

    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private CustomerStatus status;
    private CustomerSegment segment;
    private Instant createdAt;
    private Instant updatedAt;

    public Customer(
            String firstName,
            String lastName,
            String email,
            String phoneNumber,
            CustomerSegment segment) {
        this.id = UUID.randomUUID();
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.segment = segment;
        this.status = CustomerStatus.ACTIVE;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public CustomerStatus getStatus() {
        return status;
    }

    public CustomerSegment getSegment() {
        return segment;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

}
