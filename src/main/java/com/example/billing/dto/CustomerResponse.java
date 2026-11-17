package com.example.billing.dto;

import java.time.Instant;
import java.util.UUID;

import com.example.billing.domain.Customer;

public record CustomerResponse(UUID id, String name, String email, String phone, String address, Instant createdAt) {

    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(
            customer.getId(), customer.getName(), customer.getEmail(),
            customer.getPhone(), customer.getAddress(), customer.getCreatedAt());
    }
}
